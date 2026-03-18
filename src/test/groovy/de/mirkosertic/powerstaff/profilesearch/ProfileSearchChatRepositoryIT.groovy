package de.mirkosertic.powerstaff.profilesearch

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchChat
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class ProfileSearchChatRepositoryIT extends AbstractContainerBaseIT {

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM profile_search_chat WHERE creation_user = 'IT-PSC-User'").update()
    }

    def "insert und findById liefert korrekte Felder"() {
        given:
        jdbcClient.sql("""
            INSERT INTO profile_search_chat (creation_user, title)
            VALUES ('IT-PSC-User', 'Test Chat')
        """).update()
        def id = jdbcClient.sql("SELECT MAX(id) FROM profile_search_chat WHERE creation_user = 'IT-PSC-User'")
                .query(Long).single()

        when:
        def chat = jdbcClient.sql("SELECT * FROM profile_search_chat WHERE id = :id")
                .param("id", id)
                .query(ProfileSearchChat).optional()

        then:
        chat.isPresent()
        chat.get().creationUser == 'IT-PSC-User'
        chat.get().title == 'Test Chat'
    }

    def "Cascade-Delete: Chat loeschen entfernt Messages"() {
        given:
        jdbcClient.sql("INSERT INTO profile_search_chat (creation_user, title) VALUES ('IT-PSC-User', 'Cascade Test')").update()
        def chatId = jdbcClient.sql("SELECT MAX(id) FROM profile_search_chat WHERE creation_user = 'IT-PSC-User'")
                .query(Long).single()
        jdbcClient.sql("INSERT INTO profile_search_message (chat_id, role, sequence, content) VALUES (:cid, 'user', 1, 'Hallo')")
                .param("cid", chatId).update()

        when:
        jdbcClient.sql("DELETE FROM profile_search_chat WHERE id = :id").param("id", chatId).update()

        then:
        def count = jdbcClient.sql("SELECT COUNT(*) FROM profile_search_message WHERE chat_id = :cid")
                .param("cid", chatId).query(Long).single()
        count == 0
    }
}
