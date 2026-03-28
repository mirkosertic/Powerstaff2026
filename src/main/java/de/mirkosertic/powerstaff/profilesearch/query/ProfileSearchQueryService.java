package de.mirkosertic.powerstaff.profilesearch.query;

import de.mirkosertic.powerstaff.shared.ProjectStatus;
import de.mirkosertic.powerstaff.shared.query.TagView;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProfileSearchQueryService {

    private final JdbcClient jdbcClient;

    public ProfileSearchQueryService(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
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
                SELECT id, creation_date, chat_id, role, sequence, content, json_payload
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
        record Row(Long id, String code, String name1, String name2,
                   LocalDateTime lastContactDate, Long salaryPerDayLong,
                   LocalDateTime availabilityAsDate, boolean contactForbidden) {}
        final String orderBy = resolveOrderBy(criteria.sortField(), criteria.sortDir());
        final String sql = String.format("""
                SELECT f.id, f.code, f.name1, f.name2,
                       f.last_contact_date, f.salary_per_day_long,
                       f.availability_as_date, f.contact_forbidden
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
                .map(r -> new ProfileSearchResult(
                        r.id(), r.code(), r.name1(), r.name2(),
                        r.lastContactDate(), r.salaryPerDayLong(),
                        r.availabilityAsDate(), r.contactForbidden(), List.of()))
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
