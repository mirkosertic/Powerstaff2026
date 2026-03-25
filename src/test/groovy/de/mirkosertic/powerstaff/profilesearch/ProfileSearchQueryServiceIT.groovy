package de.mirkosertic.powerstaff.profilesearch

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchCommandService
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class ProfileSearchQueryServiceIT extends AbstractContainerBaseIT {

    @Autowired
    ProfileSearchQueryService queryService

    @Autowired
    ProfileSearchCommandService commandService

    @Autowired
    JdbcClient jdbcClient

    List<Long> chatIds = []

    def cleanup() {
        chatIds.each { id ->
            jdbcClient.sql("DELETE FROM profile_search_message WHERE chat_id = :cid").param("cid", id).update()
            jdbcClient.sql("DELETE FROM profile_search_chat WHERE id = :id").param("id", id).update()
        }
    }

    def "findChatsByUser liefert Chats des Users sortiert nach changed_date DESC"() {
        given:
        def id1 = commandService.createChat('IT-PSQ-User', null)
        chatIds << id1
        Thread.sleep(1100) // MySQL DATETIME has 1-second precision; need >1s gap
        def id2 = commandService.createChat('IT-PSQ-User', null)
        chatIds << id2
        commandService.addMessage(id2, 'user', 'Zweiter Chat Nachricht')

        when:
        def results = queryService.findChatsByUser('IT-PSQ-User', 0, 10)

        then:
        results.size() >= 2
        // Most recently changed (id2) should come first
        results[0].id() == id2
    }

    def "countChatsByUser gibt korrekte Anzahl zurueck"() {
        given:
        def id1 = commandService.createChat('IT-PSQ-Count-User', null)
        chatIds << id1
        def id2 = commandService.createChat('IT-PSQ-Count-User', null)
        chatIds << id2

        when:
        def count = queryService.countChatsByUser('IT-PSQ-Count-User')

        then:
        count == 2
    }

    def "findMessagesByChat liefert Nachrichten sortiert nach sequence ASC"() {
        given:
        def chatId = commandService.createChat('IT-PSQ-Msg-User', null)
        chatIds << chatId
        commandService.addMessage(chatId, 'user', 'Frage 1')
        commandService.addMessage(chatId, 'assistant', 'Antwort 1')
        commandService.addMessage(chatId, 'user', 'Frage 2')

        when:
        def messages = queryService.findMessagesByChat(chatId)

        then:
        messages.size() == 3
        messages[0].sequence() == 1
        messages[1].sequence() == 2
        messages[2].sequence() == 3
        messages[0].role() == 'user'
        messages[1].role() == 'assistant'
    }

    def "findLatestChatByUser gibt Optional mit Chat-ID zurueck"() {
        given:
        def id1 = commandService.createChat('IT-PSQ-Latest-User', null)
        chatIds << id1
        commandService.addMessage(id1, 'user', 'Alter Chat')
        Thread.sleep(1100) // MySQL DATETIME has 1-second precision; need >1s gap
        def id2 = commandService.createChat('IT-PSQ-Latest-User', null)
        chatIds << id2
        commandService.addMessage(id2, 'user', 'Neuer Chat')

        when:
        def latest = queryService.findLatestChatByUser('IT-PSQ-Latest-User')

        then:
        latest.isPresent()
        latest.get() == id2
    }

    def "findLatestChatByUser ohne Chats liefert empty"() {
        expect:
        queryService.findLatestChatByUser('not-existing-user').isEmpty()
    }

    def "findMessagesByChat gibt jsonPayload korrekt zurueck wenn gespeichert"() {
        given:
        def chatId = commandService.createChat('IT-PSQ-JP-User', null)
        chatIds << chatId
        def payload = '{"tool":"search","args":{"skills":"Java"}}'
        jdbcClient.sql("""
                INSERT INTO profile_search_message (chat_id, role, sequence, content, json_payload)
                VALUES (:chatId, :role, 1, :content, :payload)
                """)
                .param("chatId", chatId)
                .param("role", 'tool_call')
                .param("content", 'Suche starten')
                .param("payload", payload)
                .update()

        when:
        def messages = queryService.findMessagesByChat(chatId)

        then:
        messages.size() == 1
        messages[0].role() == 'tool_call'
        messages[0].jsonPayload() == payload
    }

    def "findMessagesByChat liefert null fuer jsonPayload wenn nicht gespeichert"() {
        given:
        def chatId = commandService.createChat('IT-PSQ-JP-Null-User', null)
        chatIds << chatId
        commandService.addMessage(chatId, 'user', 'Normale Nachricht ohne Payload')

        when:
        def messages = queryService.findMessagesByChat(chatId)

        then:
        messages.size() == 1
        messages[0].jsonPayload() == null
    }

    def "findMessagesByChat liefert Nachrichten mit role tool_call und tool_result korrekt"() {
        given:
        def chatId = commandService.createChat('IT-PSQ-TC-User', null)
        chatIds << chatId
        def toolCallPayload = '{"tool":"findFreelancer","args":{"skills":"Kotlin"}}'
        def toolResultPayload = '{"results":[{"code":"FL-001","name":"Max Muster"}]}'
        jdbcClient.sql("""
                INSERT INTO profile_search_message (chat_id, role, sequence, content, json_payload)
                VALUES (:chatId, 'user', 1, 'Suche Kotlin-Experten', NULL)
                """)
                .param("chatId", chatId).update()
        jdbcClient.sql("""
                INSERT INTO profile_search_message (chat_id, role, sequence, content, json_payload)
                VALUES (:chatId, 'tool_call', 2, 'Tool aufgerufen', :payload)
                """)
                .param("chatId", chatId).param("payload", toolCallPayload).update()
        jdbcClient.sql("""
                INSERT INTO profile_search_message (chat_id, role, sequence, content, json_payload)
                VALUES (:chatId, 'tool_result', 3, 'Tool-Ergebnis', :payload)
                """)
                .param("chatId", chatId).param("payload", toolResultPayload).update()

        when:
        def messages = queryService.findMessagesByChat(chatId)

        then:
        messages.size() == 3
        messages[0].role() == 'user'
        messages[0].jsonPayload() == null
        messages[1].role() == 'tool_call'
        messages[1].jsonPayload() == toolCallPayload
        messages[2].role() == 'tool_result'
        messages[2].jsonPayload() == toolResultPayload
    }

    def "buildLlmContext ohne gemerktes Projekt liefert empty"() {
        expect:
        queryService.buildLlmContext('no-remembered-project-user').isEmpty()
    }

    def "buildLlmContext mit gemerktem Projekt und Position liefert vollstaendigen Kontext"() {
        given:
        // Insert a project_position_status
        jdbcClient.sql("INSERT INTO project_position_status (description, color, color_text) VALUES ('Vorgeschlagen', '#d1fae5', '#065f46')").update()
        def statusId = jdbcClient.sql("SELECT LAST_INSERT_ID()").query(Long.class).single()

        // Insert a project
        jdbcClient.sql("""
            INSERT INTO project (db_version, project_number, status, description_short, description_long, skills, workplace, duration, stundensatz_vk)
            VALUES (0, 'IT-LLM-P001', 1, 'KI-Projekt', 'Volles Projekt', 'Java, Spring', 'Remote', '6 Monate', 150)
        """).update()
        def projectId = jdbcClient.sql("SELECT LAST_INSERT_ID()").query(Long.class).single()

        // Insert a freelancer
        jdbcClient.sql("""
            INSERT INTO freelancer (db_version, code, name1, name2, skills)
            VALUES (0, 'IT-LLM-FL01', 'Max', 'Muster', 'Java, Kotlin')
        """).update()
        def freelancerId = jdbcClient.sql("SELECT LAST_INSERT_ID()").query(Long.class).single()

        // Insert a tag and assign to freelancer
        jdbcClient.sql("INSERT INTO tags (tagname, type) VALUES ('Java', 'SCHWERPUNKT')").update()
        def tagId = jdbcClient.sql("SELECT LAST_INSERT_ID()").query(Long.class).single()
        jdbcClient.sql("INSERT INTO freelancer_tags (freelancer_id, tag_id) VALUES (:fid, :tid)")
                .param("fid", freelancerId).param("tid", tagId).update()

        // Insert project_position
        jdbcClient.sql("""
            INSERT INTO project_position (db_version, project_id, freelancer_id, status_id, konditionen, kommentar)
            VALUES (0, :pid, :fid, :sid, '100€/h', 'Top Kandidat')
        """).param("pid", projectId).param("fid", freelancerId).param("sid", statusId).update()

        // Set remembered project for test user
        jdbcClient.sql("INSERT INTO remembered_project (user_id, project_id) VALUES ('IT-LLM-User', :pid)")
                .param("pid", projectId).update()

        when:
        def ctx = queryService.buildLlmContext('IT-LLM-User')

        then:
        ctx.isPresent()
        ctx.get().projectNumber() == 'IT-LLM-P001'
        ctx.get().descriptionShort() == 'KI-Projekt'
        ctx.get().statusLabel() == 'Offen'
        ctx.get().stundensatzVk() == 150
        ctx.get().positions().size() == 1
        ctx.get().positions()[0].code() == 'IT-LLM-FL01'
        ctx.get().positions()[0].name1() == 'Max'
        ctx.get().positions()[0].tags() == ['Java']
        ctx.get().positions()[0].positionStatus() == 'Vorgeschlagen'
        ctx.get().positions()[0].konditionen() == '100€/h'

        cleanup:
        jdbcClient.sql("DELETE FROM remembered_project WHERE user_id = 'IT-LLM-User'").update()
        jdbcClient.sql("DELETE FROM project_position WHERE project_id = :pid").param("pid", projectId).update()
        jdbcClient.sql("DELETE FROM freelancer_tags WHERE freelancer_id = :fid").param("fid", freelancerId).update()
        jdbcClient.sql("DELETE FROM tags WHERE id = :tid").param("tid", tagId).update()
        jdbcClient.sql("DELETE FROM freelancer WHERE id = :fid").param("fid", freelancerId).update()
        jdbcClient.sql("DELETE FROM project WHERE id = :pid").param("pid", projectId).update()
        jdbcClient.sql("DELETE FROM project_position_status WHERE id = :sid").param("sid", statusId).update()
    }
}
