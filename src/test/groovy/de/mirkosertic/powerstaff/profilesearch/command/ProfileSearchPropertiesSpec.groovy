package de.mirkosertic.powerstaff.profilesearch.command

import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration

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

    def "streamingTimeout hat den Default-Wert von 5 Minuten"() {
        expect:
        properties.getStreamingTimeout() == Duration.ofMinutes(5)
    }

    def "streamingTimeout kann ueberschrieben werden"() {
        when:
        properties.setStreamingTimeout(Duration.ofMinutes(10))

        then:
        properties.getStreamingTimeout() == Duration.ofMinutes(10)
    }
}
