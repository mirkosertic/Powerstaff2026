package de.mirkosertic.powerstaff.shared

import spock.lang.Specification

class ProjectStatusSpec extends Specification {

    def "getLabel returns readable label for all values"() {
        expect:
        status.getLabel() == expectedLabel

        where:
        status                      | expectedLabel
        ProjectStatus.OFFEN         | "Offen"
        ProjectStatus.VERLOREN      | "Verloren"
        ProjectStatus.CANCELED      | "Storniert"
        ProjectStatus.BESETZT       | "Besetzt"
        ProjectStatus.SEARCH_ZU     | "Suche abgeschlossen"
    }

    def "fromInt returns correct enum value for known codes"() {
        expect:
        ProjectStatus.fromInt(1) == ProjectStatus.OFFEN
        ProjectStatus.fromInt(2) == ProjectStatus.VERLOREN
        ProjectStatus.fromInt(3) == ProjectStatus.CANCELED
        ProjectStatus.fromInt(4) == ProjectStatus.BESETZT
        ProjectStatus.fromInt(5) == ProjectStatus.SEARCH_ZU
    }

    def "fromInt throws IllegalArgumentException for unknown code"() {
        when:
        ProjectStatus.fromInt(99)

        then:
        thrown(IllegalArgumentException)
    }

    def "getCode returns correct numeric code"() {
        expect:
        status.getCode() == expectedCode

        where:
        status                      | expectedCode
        ProjectStatus.OFFEN         | 1
        ProjectStatus.VERLOREN      | 2
        ProjectStatus.CANCELED      | 3
        ProjectStatus.BESETZT       | 4
        ProjectStatus.SEARCH_ZU     | 5
    }
}
