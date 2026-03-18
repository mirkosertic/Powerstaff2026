package de.mirkosertic.powerstaff.profilesearch

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class ProfileSearchMessageRepositoryIT extends AbstractContainerBaseIT {

    @Autowired
    JdbcClient jdbcClient

    Long chatId

    def setup() {
        jdbcClient.sql("INSERT INTO profile_search_chat (creation_user, title) VALUES ('IT-PSM-User', 'PSM Test Chat')").update()
        chatId = jdbcClient.sql("SELECT MAX(id) FROM profile_search_chat WHERE creation_user = 'IT-PSM-User'")
                .query(Long).single()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM profile_search_message WHERE chat_id = :cid").param("cid", chatId).update()
        jdbcClient.sql("DELETE FROM profile_search_chat WHERE creation_user = 'IT-PSM-User'").update()
    }

    def "insert und findByChatId liefert Nachrichten"() {
        given:
        jdbcClient.sql("INSERT INTO profile_search_message (chat_id, role, sequence, content) VALUES (:cid, 'user', 1, 'Frage')")
                .param("cid", chatId).update()
        jdbcClient.sql("INSERT INTO profile_search_message (chat_id, role, sequence, content) VALUES (:cid, 'assistant', 2, 'Antwort')")
                .param("cid", chatId).update()

        when:
        def messages = jdbcClient.sql("SELECT * FROM profile_search_message WHERE chat_id = :cid ORDER BY sequence ASC")
                .param("cid", chatId)
                .query(ProfileSearchMessage).list()

        then:
        messages.size() == 2
        messages[0].role == 'user'
        messages[0].sequence == 1
        messages[1].role == 'assistant'
        messages[1].sequence == 2
    }

    def "findByChatId fuer unbekannten Chat liefert leere Liste"() {
        expect:
        jdbcClient.sql("SELECT * FROM profile_search_message WHERE chat_id = -1")
                .query(ProfileSearchMessage).list().isEmpty()
    }
}
