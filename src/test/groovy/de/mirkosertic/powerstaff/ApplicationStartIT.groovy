package de.mirkosertic.powerstaff

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

@SpringBootTest
class ApplicationStartIT extends AbstractContainerBaseIT {

    @Autowired
    ApplicationContext applicationContext

    def "Spring-Kontext startet vollstaendig ohne Fehler"() {
        expect:
        applicationContext != null
    }
}
