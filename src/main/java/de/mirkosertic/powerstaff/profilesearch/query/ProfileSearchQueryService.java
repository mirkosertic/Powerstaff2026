package de.mirkosertic.powerstaff.profilesearch.query;

import de.mirkosertic.powerstaff.profilesearch.command.McpClientFactory;
import de.mirkosertic.powerstaff.profilesearch.command.McpConnectionException;
import de.mirkosertic.powerstaff.profilesearch.command.McpConnectionProperties;
import de.mirkosertic.powerstaff.shared.ProjectStatus;
import de.mirkosertic.powerstaff.shared.query.TagView;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.thymeleaf.util.ArrayUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProfileSearchQueryService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileSearchQueryService.class);

    static final String MCP_EXTENDED_SEARCH_TOOL_NAME = "extendedSearch";
    static final String MCP_SEMANTIC_SEARCH_TOOL_NAME = "semanticSearch";

    private final JdbcClient jdbcClient;
    private final McpClientFactory mcpClientFactory;
    private final McpConnectionProperties mcpConnectionProperties;
    private final ObjectMapper objectMapper;

    public ProfileSearchQueryService(final JdbcClient jdbcClient,
                                       final McpClientFactory mcpClientFactory,
                                       final McpConnectionProperties mcpConnectionProperties,
                                       final ObjectMapper objectMapper) {
        this.jdbcClient = jdbcClient;
        this.mcpClientFactory = mcpClientFactory;
        this.mcpConnectionProperties = mcpConnectionProperties;
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

    public ProfileSearchPage searchFreelancers(final ProfileSearchCriteria criteria, final int offset, final int limit) {
        // MCP deaktiviert → DB-Fallback (nur für E2E-Tests)
        if (!mcpConnectionProperties.isEnabled()) {
            logger.debug("MCP deaktiviert – Suche über DB");
            return searchFreelancersViaDb(criteria, offset, limit);
        }

        // MCP enabled → Suche mit Retry, bei Fehler Exception an User
        try (final McpSyncClient client = mcpClientFactory.createClient()) {
            return searchFreelancersViaMcp(client, criteria, offset, limit);
        } catch (final McpConnectionException e) {
            // Nach Retries fehlgeschlagen → User-Fehler
            logger.error("MCP-Suche fehlgeschlagen nach {} Versuchen",
                    mcpConnectionProperties.getMaxRetries() + 1, e);
            throw new McpSearchException(
                    "Die Profilsuche ist momentan nicht verfügbar. Bitte versuchen Sie es später erneut.",
                    e
            );
        } catch (final Exception e) {
            // Unerwarteter Fehler (z.B. Tool-Call, Parsing)
            logger.error("Unerwarteter Fehler bei MCP-Suche", e);
            throw new McpSearchException("Profilsuche fehlgeschlagen.", e);
        }
    }

    // ── MCP-Pfad ──────────────────────────────────────────────────────────────

    /**
     * Ruft das MCP-Search-Tool auf und konvertiert das Ergebnis in {@link ProfileSearchResult}-Objekte.
     * Voraussetzung: Client ist bereits initialisiert und verfügt über Search-Tools.
     */
    private ProfileSearchPage searchFreelancersViaMcp(final McpSyncClient client,
                                                       final ProfileSearchCriteria criteria,
                                                       final int offset, final int limit) {
        // 1. Verifiziere dass Client Search-Tools anbietet
        final McpSchema.ListToolsResult toolsResult = client.listTools();
        final boolean hasSearchTool = toolsResult.tools().stream()
                .anyMatch(t -> MCP_EXTENDED_SEARCH_TOOL_NAME.equals(t.name())
                        || MCP_SEMANTIC_SEARCH_TOOL_NAME.equals(t.name()));

        if (!hasSearchTool) {
            logger.error("MCP-Client bietet kein Search-Tool an. Verfügbare Tools: {}",
                    toolsResult.tools().stream().map(McpSchema.Tool::name).toList());
            throw new McpSearchException("MCP-Server bietet kein Search-Tool an.", null);
        }

        // 2. Tool aufrufen
        final Map<String, Object> arguments = buildMcpSearchArguments(criteria, offset, limit);
        final McpSchema.CallToolResult toolResult;
        if (criteria.isSemanticSearchActive()) {
            logger.info("Performing semantic search via MCP-Server");
            toolResult = client.callTool(
                    new McpSchema.CallToolRequest(MCP_SEMANTIC_SEARCH_TOOL_NAME, arguments));
        } else {
            logger.info("Performing extended search via MCP-Server");
            toolResult = client.callTool(
                    new McpSchema.CallToolRequest(MCP_EXTENDED_SEARCH_TOOL_NAME, arguments));
        }

        // 3. Fehlerbehandlung
        if (Boolean.TRUE.equals(toolResult.isError())) {
            logger.error("MCP Search-Tool meldete einen Fehler: {}", toolResult.content());
            throw new McpSearchException("MCP-Suche lieferte Fehler.", null);
        }

        // 4. Ergebnis parsen
        return parseMcpSearchResult(toolResult, criteria.isSemanticSearchActive());

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
        arguments.put("page", offset / limit);
        if (criteria.isSemanticSearchActive()) {
            arguments.put("similarityThreshold", 0.75f);
        }

        final List<Object> filters = new ArrayList<>();
        if (criteria.salaryPerDayFrom() != null || criteria.salaryPerDayTo() != null) {
            final Map<String, Object> filter = new HashMap<>();
            filter.put("field", "dbmeta_tagessatz");
            filter.put("operator", "range");
            if (criteria.salaryPerDayFrom() != null) {
                filter.put("from", criteria.salaryPerDayFrom().toString());
            }
            if (criteria.salaryPerDayTo() != null) {
                filter.put("to", criteria.salaryPerDayTo().toString());
            }
            filters.add(filter);
        }
        if (criteria.tagIds() != null && !criteria.tagIds().isEmpty()) {
            final String[] tagIds = StringUtils.split(criteria.tagIds(),",");
            final Map<String, Object> filter = new HashMap<>();
            filter.put("field", "dbmeta_tags");
            filter.put("operator", "in");
            filter.put("values", tagIds);
            filters.add(filter);
        }

        arguments.put("filters", filters);

        if (!Objects.isNull(criteria.sortField())) {
            final Map<String, String> sortMap = new HashMap<>();
            sortMap.put("name1", "dbmeta_name1");
            sortMap.put("name2", "dbmeta_name2");
            sortMap.put("lastContactDate", "dbmeta_name2");
            sortMap.put("salaryPerDayLong", "dbmeta_tagessatz");
            sortMap.put("availabilityAsDate", "dbmeta_availability_as_date");
            sortMap.put("code", "dbmeta_code");

            final String indexField = sortMap.get(criteria.sortField());
            if (indexField != null) {
                arguments.put("sortBy", indexField);
                if ("ASC".equalsIgnoreCase(criteria.sortDir())) {
                    arguments.put("sortOrder", "asc");;
                } else if ("DESC".equalsIgnoreCase(criteria.sortDir())) {
                    arguments.put("sortOrder", "desc");;
                } else {
                    logger.warn("Unbekannter Sortierreihenfolge: {}", criteria.sortDir());
                }
            } else {
                logger.warn("Kein Indexfeld gefunden für : {}", criteria.sortField());
            }
        }

        return arguments;
    }

    /**
     * Konvertiert das Ergebnis des MCP-Search-Tools in eine Liste von {@link ProfileSearchResult}.
     */
    private ProfileSearchPage parseMcpSearchResult(final McpSchema.CallToolResult toolResult, boolean isSemanticSearch) {
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

        record DocumentEntry(String code, String serp, double score) {}

        final List<DocumentEntry> entries = new ArrayList<>();
        long totalHits = 0L;
        for (final McpSchema.Content content : toolResult.content()) {
            if (content instanceof final McpSchema.TextContent textContent) {
                final SearchResponse response = objectMapper.readValue(textContent.text(), SearchResponse.class);
                if (response.success()) {
                    totalHits = response.totalHits();
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
                        entries.add(new DocumentEntry(code, serp.toString(), document.score()));
                    }
                } else {
                    logger.error("MCP-Search-Tool meldete einen Fehler: {}", response.error());
                }
            } else {
                logger.warn("Nicht unterstützter Content-Typ: {} in MCP-Antwort", content.getClass().getSimpleName());
            }
        }

        if (entries.isEmpty()) {
            return new ProfileSearchPage(List.of(), totalHits);
        }

        // Batch-Load: alle Freiberuflerdaten und Tags in je einem SELECT statt n Einzelabfragen
        final List<String> codes = entries.stream().map(DocumentEntry::code).toList();
        final Map<String, FreelancerBatchRow> freelancerByCode = findFreelancersByCodesInBatch(codes);

        final List<Long> freelancerIds = freelancerByCode.values().stream()
                .map(FreelancerBatchRow::id)
                .toList();
        final Map<Long, List<TagView>> tagsByFreelancerId = findTagsByFreelancerIdsInBatch(freelancerIds);

        final double maxScore = entries.stream().mapToDouble(DocumentEntry::score).max().orElse(1.0);

        final List<ProfileSearchResult> results = new ArrayList<>();
        for (final DocumentEntry entry : entries) {
            final double scoreRelative = maxScore > 0 ? entry.score() / maxScore : 0.0;
            final FreelancerBatchRow freelancer = freelancerByCode.get(entry.code());
            if (freelancer == null) {
                logger.warn("Kein Freiberufler mit Code '{}' gefunden – MCP-Treffer wird ohne DB-Daten übernommen", entry.code());
                results.add(new ProfileSearchResult(null, entry.code(), null, null, null, null, null, false, List.of(), entry.serp(), !isSemanticSearch, scoreRelative));
            } else {
                final List<TagView> tags = tagsByFreelancerId.getOrDefault(freelancer.id(), List.of());
                results.add(new ProfileSearchResult(
                        freelancer.id(), entry.code(), freelancer.name1(), freelancer.name2(),
                        freelancer.lastContactDate(), freelancer.salaryPerDayLong(),
                        freelancer.availabilityAsDate(), freelancer.contactForbidden(),
                        tags, entry.serp(), !isSemanticSearch, scoreRelative));
            }
        }
        return new ProfileSearchPage(results, totalHits);
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

    private ProfileSearchPage searchFreelancersViaDb(final ProfileSearchCriteria criteria,
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
        final List<Row> rows = jdbcClient.sql(sql)
                .param("limit", limit)
                .param("offset", offset)
                .query(Row.class)
                .list();

        // Batch-Load Tags für alle gefundenen Freiberufler (analog zum MCP-Pfad)
        final List<Long> freelancerIds = rows.stream().map(Row::id).toList();
        final Map<Long, List<TagView>> tagsByFreelancerId = findTagsByFreelancerIdsInBatch(freelancerIds);

        final List<ProfileSearchResult> results = rows.stream()
                .map(r -> {
                    final String term = criteria.searchTerm() != null && !criteria.searchTerm().isBlank()
                            ? " – Suche: **" + criteria.searchTerm() + "**"
                            : "";
                    final String serp = "**" + r.name1() + " " + r.name2() + "**" + term;
                    final List<TagView> tags = tagsByFreelancerId.getOrDefault(r.id(), List.of());
                    return new ProfileSearchResult(
                            r.id(), r.code(), r.name1(), r.name2(),
                            r.lastContactDate(), r.salaryPerDayLong(),
                            r.availabilityAsDate(), r.contactForbidden(), tags, serp, true, null);
                })
                .toList();
        final long total = jdbcClient.sql("SELECT COUNT(*) FROM freelancer")
                .query(Long.class)
                .single();
        return new ProfileSearchPage(results, total);
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

}
