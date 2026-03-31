package de.mirkosertic.powerstaff.profilesearch.query;

import de.mirkosertic.powerstaff.shared.ProjectStatus;
import de.mirkosertic.powerstaff.shared.query.TagView;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProfileSearchQueryService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileSearchQueryService.class);

    static final String MCP_SEARCH_TOOL_NAME = "search";

    private final JdbcClient jdbcClient;
    private final List<McpSyncClient> mcpClients;
    private final ObjectMapper objectMapper;

    public ProfileSearchQueryService(final JdbcClient jdbcClient, final List<McpSyncClient> mcpClients, final ObjectMapper objectMapper) {
        this.jdbcClient = jdbcClient;
        this.mcpClients = mcpClients;
        this.objectMapper = objectMapper;
    }

    public List<ChatListView> findChatsByUser(final String userId, final int offset, final int limit) {
        return jdbcClient.sql("""
                SELECT c.id, c.creation_date, c.creation_user, c.changed_date, c.title,
                       c.project_id, p.project_number
                FROM profile_search_chat c
                LEFT JOIN project p ON p.id = c.project_id
                WHERE c.creation_user = :userId
                ORDER BY c.changed_date DESC
                LIMIT :limit OFFSET :offset
                """)
                .param("userId", userId)
                .param("limit", limit)
                .param("offset", offset)
                .query(ChatListView.class)
                .list();
    }

    public long countChatsByUser(final String userId) {
        return jdbcClient.sql("SELECT COUNT(*) FROM profile_search_chat WHERE creation_user = :userId")
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public List<MessageView> findMessagesByChat(final Long chatId) {
        return jdbcClient.sql("""
                SELECT id, creation_date, chat_id, role, sequence, content, json_payload, assistant_thoughts
                FROM profile_search_message
                WHERE chat_id = :chatId
                ORDER BY sequence ASC
                """)
                .param("chatId", chatId)
                .query(MessageView.class)
                .list();
    }

    public Optional<Long> findLatestChatByUser(final String userId) {
        return jdbcClient.sql("""
                SELECT id FROM profile_search_chat
                WHERE creation_user = :userId
                ORDER BY changed_date DESC
                LIMIT 1
                """)
                .param("userId", userId)
                .query(Long.class)
                .optional();
    }


    public Optional<LlmProjectContext> buildLlmContext(final String userId) {
        // Step 1: find remembered project for user
        final var projectIdOpt = jdbcClient.sql(
                        "SELECT project_id FROM remembered_project WHERE user_id = :userId")
                .param("userId", userId)
                .query(Long.class)
                .optional();

        if (projectIdOpt.isEmpty()) {
            return Optional.empty();
        }
        final long projectId = projectIdOpt.get();

        // Step 2: load project fields
        record ProjectRow(String projectNumber, String descriptionShort, String descriptionLong,
                          String workplace, String skills, String duration,
                          LocalDateTime startDate, int status, Long stundensatzVk) {}

        final var projectOpt = jdbcClient.sql("""
                SELECT project_number, description_short, description_long, workplace, skills,
                       duration, start_date, status, stundensatz_vk
                FROM project WHERE id = :projectId
                """)
                .param("projectId", projectId)
                .query(ProjectRow.class)
                .optional();

        if (projectOpt.isEmpty()) {
            return Optional.empty();
        }
        final var project = projectOpt.get();

        // Step 3: load positions with freelancer and status info
        record PositionRow(Long freelancerId, String code, String name1, String name2,
                           String freelancerSkills, String positionStatus,
                           String konditionen, String kommentar) {}

        final var positionRows = jdbcClient.sql("""
                SELECT pp.freelancer_id, f.code, f.name1, f.name2, f.skills AS freelancer_skills,
                       pps.description AS position_status, pp.konditionen, pp.kommentar
                FROM project_position pp
                JOIN freelancer f ON f.id = pp.freelancer_id
                LEFT JOIN project_position_status pps ON pps.id = pp.status_id
                WHERE pp.project_id = :projectId
                """)
                .param("projectId", projectId)
                .query(PositionRow.class)
                .list();

        // Step 4: for each position, load tags
        final List<LlmFreelancerContext> positions = new ArrayList<>();
        for (final var pos : positionRows) {
            final var tags = jdbcClient.sql("""
                    SELECT t.tagname FROM tags t
                    JOIN freelancer_tags ft ON ft.tag_id = t.id
                    WHERE ft.freelancer_id = :freelancerId
                    ORDER BY t.tagname
                    """)
                    .param("freelancerId", pos.freelancerId())
                    .query(String.class)
                    .list();

            positions.add(new LlmFreelancerContext(
                    pos.code(), pos.name1(), pos.name2(), pos.freelancerSkills(),
                    tags, pos.positionStatus(), pos.konditionen(), pos.kommentar()
            ));
        }

        String statusLabel;
        try {
            statusLabel = ProjectStatus.fromInt(project.status()).getLabel();
        } catch (final IllegalArgumentException e) {
            statusLabel = String.valueOf(project.status());
        }

        return Optional.of(new LlmProjectContext(
                project.projectNumber(), project.descriptionShort(), project.descriptionLong(),
                project.workplace(), project.skills(), project.duration(), project.startDate(),
                statusLabel, project.stundensatzVk(), positions
        ));
    }

    public List<ProfileSearchResult> searchFreelancers(final ProfileSearchCriteria criteria, final int offset, final int limit) {
        final Optional<McpSyncClient> mcpClient = findMcpClientWithSearchTool();
        if (mcpClient.isPresent()) {
            logger.debug("MCP '{}'-Tool gefunden – delegiere Suche an MCP-Server", MCP_SEARCH_TOOL_NAME);
            return searchFreelancersViaMcp(mcpClient.get(), criteria, offset, limit);
        }
        logger.debug("Kein MCP '{}'-Tool verfügbar – Suche über DB", MCP_SEARCH_TOOL_NAME);
        return searchFreelancersViaDb(criteria, offset, limit);
    }

    // ── MCP-Pfad ──────────────────────────────────────────────────────────────

    /** Sucht den ersten MCP-Client, der ein Tool mit dem Namen {@value #MCP_SEARCH_TOOL_NAME} anbietet. */
    private Optional<McpSyncClient> findMcpClientWithSearchTool() {
        for (final McpSyncClient client : mcpClients) {
            try {
                final McpSchema.ListToolsResult result = client.listTools();
                final boolean hasSearch = result.tools().stream()
                        .anyMatch(t -> MCP_SEARCH_TOOL_NAME.equals(t.name()));
                if (hasSearch) {
                    return Optional.of(client);
                }
            } catch (final Exception e) {
                logger.warn("Fehler beim Abfragen der Tools von MCP-Client {}: {}", client, e.getMessage());
            }
        }
        return Optional.empty();
    }

    /** Ruft das MCP-Search-Tool auf und konvertiert das Ergebnis in {@link ProfileSearchResult}-Objekte. */
    private List<ProfileSearchResult> searchFreelancersViaMcp(final McpSyncClient client,
                                                               final ProfileSearchCriteria criteria,
                                                               final int offset, final int limit) {
        final Map<String, Object> arguments = buildMcpSearchArguments(criteria, offset, limit);
        final McpSchema.CallToolResult toolResult = client.callTool(
                new McpSchema.CallToolRequest(MCP_SEARCH_TOOL_NAME, arguments));
        if (Boolean.TRUE.equals(toolResult.isError())) {
            logger.error("MCP '{}'-Tool meldete einen Fehler: {}", MCP_SEARCH_TOOL_NAME, toolResult.content());
            return List.of();
        }
        return parseMcpSearchResult(toolResult);
    }

    /**
     * Baut die Argument-Map für das MCP-Search-Tool aus den Suchkriterien.
     */
    private Map<String, Object> buildMcpSearchArguments(final ProfileSearchCriteria criteria,
                                                          final int offset, final int limit) {

        final Map<String, Object> arguments = new HashMap<>();
        arguments.put("query", criteria.searchTerm());
        arguments.put("sortBy", "_score");
        arguments.put("pageSize", limit);
        arguments.put("page", 0); //
        arguments.put("useVectorSearch", criteria.semanticSearch() != null ? criteria.semanticSearch() : false);
        return arguments;
    }

    /**
     * Konvertiert das Ergebnis des MCP-Search-Tools in eine Liste von {@link ProfileSearchResult}.
     */
    private List<ProfileSearchResult> parseMcpSearchResult(final McpSchema.CallToolResult toolResult) {
        record Passage(
                String text,
                double score,
                List<String> matchedTerms,
                double termCoverage,
                double position
        ) {
        }

        record VectorMatchInfo(
                boolean matchedViaVector,
                int matchedChunkIndex,
                String matchedChunkText,
                float vectorScore
        ) {
        }

        record SearchDocument(
                double score,
                String filePath,
                String fileName,
                String title,
                String author,
                String creator,
                String subject,
                String language,
                String fileExtension,
                String fileType,
                Long fileSize,
                Long createdDate,
                Long modifiedDate,
                Long indexedDate,
                List<Passage> passages,
                VectorMatchInfo vectorMatchInfo
        ) {
        }

        record FacetValue(String value, long count) {
        }

        record ActiveFilter(
                String field,
                String operator,
                String value,
                List<String> values,
                String from,
                String to,
                String addedAt,
                long matchCount
        ) {
        }

        record SearchResponse(
                boolean success,
                List<SearchDocument> documents,
                long totalHits,
                int page,
                int pageSize,
                int totalPages,
                boolean hasNextPage,
                boolean hasPreviousPage,
                Map<String, List<FacetValue>> facets,
                List<ActiveFilter> activeFilters,
                long searchTimeMs,
                String contentNote,
                String error
        ) {}

        record DocumentEntry(String code, String serp) {}

        final List<DocumentEntry> entries = new ArrayList<>();
        for (final McpSchema.Content content : toolResult.content()) {
            if (content instanceof final McpSchema.TextContent textContent) {
                final SearchResponse response = objectMapper.readValue(textContent.text(), SearchResponse.class);
                if (response.success()) {
                    for (final SearchDocument document : response.documents()) {
                        String code = document.fileName();
                        if (code.indexOf("Profil ") == 0) {
                            code = code.substring("Profil ".length());
                        }
                        final int p = code.indexOf(".");
                        if (p > 0) {
                            code = code.substring(0, p);
                        }

                        final StringBuilder serp = new StringBuilder();
                        for (final Passage passage : document.passages()) {
                            serp.append(passage.text()).append(" ");
                        }
                        entries.add(new DocumentEntry(code, serp.toString()));
                    }
                } else {
                    logger.error("MCP-Search-Tool meldete einen Fehler: {}", response.error());
                }
            } else {
                logger.warn("Nicht unterstützter Content-Typ: {} in MCP-Antwort", content.getClass().getSimpleName());
            }
        }

        if (entries.isEmpty()) {
            return List.of();
        }

        // Batch-Load: alle Freiberuflerdaten und Tags in je einem SELECT statt n Einzelabfragen
        final List<String> codes = entries.stream().map(DocumentEntry::code).toList();
        final Map<String, FreelancerBatchRow> freelancerByCode = findFreelancersByCodesInBatch(codes);

        final List<Long> freelancerIds = freelancerByCode.values().stream()
                .map(FreelancerBatchRow::id)
                .toList();
        final Map<Long, List<TagView>> tagsByFreelancerId = findTagsByFreelancerIdsInBatch(freelancerIds);

        final List<ProfileSearchResult> results = new ArrayList<>();
        for (final DocumentEntry entry : entries) {
            final FreelancerBatchRow freelancer = freelancerByCode.get(entry.code());
            if (freelancer == null) {
                logger.warn("Kein Freiberufler mit Code '{}' gefunden – MCP-Treffer wird ohne DB-Daten übernommen", entry.code());
                results.add(new ProfileSearchResult(null, entry.code(), null, null, null, null, null, false, List.of(), entry.serp()));
            } else {
                final List<TagView> tags = tagsByFreelancerId.getOrDefault(freelancer.id(), List.of());
                results.add(new ProfileSearchResult(
                        freelancer.id(), entry.code(), freelancer.name1(), freelancer.name2(),
                        freelancer.lastContactDate(), freelancer.salaryPerDayLong(),
                        freelancer.availabilityAsDate(), freelancer.contactForbidden(),
                        tags, entry.serp()));
            }
        }
        return results;
    }

    /**
     * Lädt Freiberuflerdaten für eine Liste von Codes in einer einzigen DB-Abfrage.
     * Package-private für Tests.
     */
    Map<String, FreelancerBatchRow> findFreelancersByCodesInBatch(final List<String> codes) {
        if (codes.isEmpty()) {
            return Map.of();
        }
        return jdbcClient.sql("""
                SELECT id, code, name1, name2,
                       last_contact_date, salary_per_day_long,
                       availability_as_date, contactforbidden
                FROM freelancer
                WHERE code IN (:codes)
                """)
                .param("codes", codes)
                .query(FreelancerBatchRow.class)
                .list()
                .stream()
                .collect(Collectors.toMap(FreelancerBatchRow::code, r -> r));
    }

    /**
     * Lädt Tags für eine Liste von Freiberufler-IDs in einer einzigen DB-Abfrage.
     * Package-private für Tests.
     */
    Map<Long, List<TagView>> findTagsByFreelancerIdsInBatch(final List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        record TagRow(Long freelancerId, Long tagId, String tagname, String type) {}
        return jdbcClient.sql("""
                SELECT ft.freelancer_id, t.id AS tag_id, t.tagname, t.type
                FROM freelancer_tags ft
                JOIN tags t ON t.id = ft.tag_id
                WHERE ft.freelancer_id IN (:ids)
                ORDER BY t.tagname
                """)
                .param("ids", ids)
                .query(TagRow.class)
                .list()
                .stream()
                .collect(Collectors.groupingBy(
                        TagRow::freelancerId,
                        Collectors.mapping(r -> new TagView(r.tagId(), r.tagname(), r.type()), Collectors.toList())));
    }

    // ── DB-Pfad (bisherige Implementierung) ───────────────────────────────────

    private List<ProfileSearchResult> searchFreelancersViaDb(final ProfileSearchCriteria criteria,
                                                              final int offset, final int limit) {
        record Row(Long id, String code, String name1, String name2,
                   LocalDateTime lastContactDate, Long salaryPerDayLong,
                   LocalDateTime availabilityAsDate, boolean contactForbidden) {}
        final String orderBy = resolveOrderBy(criteria.sortField(), criteria.sortDir());
        final String sql = String.format("""
                SELECT f.id, f.code, f.name1, f.name2,
                       f.last_contact_date, f.salary_per_day_long,
                       f.availability_as_date, f.contactforbidden
                FROM freelancer f
                ORDER BY %s
                LIMIT :limit OFFSET :offset
                """, orderBy);
        return jdbcClient.sql(sql)
                .param("limit", limit)
                .param("offset", offset)
                .query(Row.class)
                .list()
                .stream()
                .map(r -> {
                    final String term = criteria.searchTerm() != null && !criteria.searchTerm().isBlank()
                            ? " – Suche: **" + criteria.searchTerm() + "**"
                            : "";
                    final String serp = "**" + r.name1() + " " + r.name2() + "**" + term;
                    return new ProfileSearchResult(
                            r.id(), r.code(), r.name1(), r.name2(),
                            r.lastContactDate(), r.salaryPerDayLong(),
                            r.availabilityAsDate(), r.contactForbidden(), List.of(), serp);
                })
                .toList();
    }

    private String resolveOrderBy(final String sortField, final String sortDir) {
        final String column = switch (sortField != null ? sortField : "") {
            case "name2"            -> "f.name2";
            case "lastContactDate"  -> "f.last_contact_date";
            case "salaryPerDayLong" -> "f.salary_per_day_long";
            case "availabilityAsDate" -> "f.availability_as_date";
            case "code"             -> "f.code";
            default                 -> "f.name1";
        };
        final String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        return column + " " + direction;
    }

    public long countSearchFreelancers(final ProfileSearchCriteria criteria) {
        return jdbcClient.sql("SELECT COUNT(*) FROM freelancer")
                .query(Long.class)
                .single();
    }
}
