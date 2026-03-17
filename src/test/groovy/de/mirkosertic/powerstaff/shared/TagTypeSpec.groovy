package de.mirkosertic.powerstaff.shared

import spock.lang.Specification

class TagTypeSpec extends Specification {

    def "getLabel returns readable label for all values"() {
        expect:
        tagType.getLabel() == expectedLabel

        where:
        tagType               | expectedLabel
        TagType.SCHWERPUNKT   | "Schwerpunkt"
        TagType.FUNKTION      | "Funktion"
        TagType.EINSATZORT    | "Einsatzort"
        TagType.BEMERKUNG     | "Bemerkung"
        TagType.TYP           | "Typ"
    }

    def "getOrder returns correct order for all values"() {
        expect:
        tagType.getOrder() == expectedOrder

        where:
        tagType               | expectedOrder
        TagType.SCHWERPUNKT   | 0
        TagType.FUNKTION      | 1
        TagType.EINSATZORT    | 2
        TagType.BEMERKUNG     | 3
        TagType.TYP           | 4
    }
}
