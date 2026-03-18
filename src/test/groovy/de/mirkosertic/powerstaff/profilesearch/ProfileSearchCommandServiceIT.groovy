package de.mirkosertic.powerstaff.profilesearch

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchCommandService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class ProfileSearchCommandServiceIT extends AbstractContainerBaseIT {

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

    def "createChat legt neuen Chat an und gibt ID zurueck"() {
        when:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId

        then:
        chatId != null
        def count = jdbcClient.sql("SELECT COUNT(*) FROM profile_search_chat WHERE id = :id").param("id", chatId).query(Long).single()
        count == 1
    }

    def "deleteChat loescht Chat inkl. Messages (Cascade)"() {
        given:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId
        commandService.addMessage(chatId, 'user', 'Hallo')

        when:
        commandService.deleteChat(chatId)
        chatIds.remove(chatId)

        then:
        def chatCount = jdbcClient.sql("SELECT COUNT(*) FROM profile_search_chat WHERE id = :id").param("id", chatId).query(Long).single()
        def msgCount = jdbcClient.sql("SELECT COUNT(*) FROM profile_search_message WHERE chat_id = :cid").param("cid", chatId).query(Long).single()
        chatCount == 0
        msgCount == 0
    }

    def "addMessage setzt korrekte Sequenznummern"() {
        given:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId

        when:
        def msg1 = commandService.addMessage(chatId, 'user', 'Erste Nachricht')
        def msg2 = commandService.addMessage(chatId, 'assistant', 'Zweite Nachricht')

        then:
        msg1.sequence == 1
        msg2.sequence == 2
    }

    def "addMessage mit erster User-Nachricht generiert Titel (max 60 Zeichen)"() {
        given:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId
        def longText = 'A' * 80

        when:
        commandService.addMessage(chatId, 'user', longText)

        then:
        def title = jdbcClient.sql("SELECT title FROM profile_search_chat WHERE id = :id").param("id", chatId).query(String).single()
        title.length() == 60
        title == 'A' * 60
    }

    def "addMessage aktualisiert changedDate in Chat"() {
        given:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId

        when:
        commandService.addMessage(chatId, 'user', 'Test')

        then:
        def changedDate = jdbcClient.sql("SELECT changed_date FROM profile_search_chat WHERE id = :id")
                .param("id", chatId).query(Object).single()
        changedDate != null
    }
}
