package de.mirkosertic.powerstaff.profilesearch.command

import spock.lang.Specification
import spock.lang.Subject

class ProfileSearchPropertiesSpec extends Specification {

    @Subject
    ProfileSearchProperties properties = new ProfileSearchProperties()

    def "maxContextTokens hat den Default-Wert 128000"() {
        expect:
        properties.getMaxContextTokens() == 128000
    }

    def "maxContextTokens kann ueberschrieben werden"() {
        when:
        properties.setMaxContextTokens(32000)

        then:
        properties.getMaxContextTokens() == 32000
    }
}
