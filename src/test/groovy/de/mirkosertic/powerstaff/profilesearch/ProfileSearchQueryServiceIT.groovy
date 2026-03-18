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
}
