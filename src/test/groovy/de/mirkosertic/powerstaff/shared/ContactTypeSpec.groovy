package de.mirkosertic.powerstaff.shared

import spock.lang.Specification

class ContactTypeSpec extends Specification {

    def "getLabel returns readable label for all values"() {
        expect:
        contactType.getLabel() == expectedLabel

        where:
        contactType          | expectedLabel
        ContactType.EMAIL    | "E-Mail"
        ContactType.WEB      | "Website"
        ContactType.XING     | "XING"
        ContactType.GULP     | "GULP"
        ContactType.TELEFON  | "Telefon"
        ContactType.FAX      | "Fax"
    }

    def "buildLink for EMAIL returns mailto link"() {
        expect:
        ContactType.EMAIL.buildLink("user@example.com") == "mailto:user@example.com"
    }

    def "buildLink for TELEFON returns tel link"() {
        expect:
        ContactType.TELEFON.buildLink("+49123456789") == "tel:+49123456789"
    }

    def "buildLink for WEB returns value directly when it starts with http"() {
        expect:
        ContactType.WEB.buildLink("https://www.example.com") == "https://www.example.com"
    }

    def "buildLink for WEB prepends https when value does not start with http"() {
        expect:
        ContactType.WEB.buildLink("www.example.com") == "https://www.example.com"
    }

    def "buildLink for XING returns value directly when it starts with http"() {
        expect:
        ContactType.XING.buildLink("https://www.xing.com/profile/john_doe") == "https://www.xing.com/profile/john_doe"
    }

    def "buildLink for XING prepends xing profile URL when value does not start with http"() {
        expect:
        ContactType.XING.buildLink("john_doe") == "https://www.xing.com/profile/john_doe"
    }

    def "buildLink for GULP returns value directly when it starts with http"() {
        expect:
        ContactType.GULP.buildLink("https://www.gulp.de/gulp2/g/profil/12345") == "https://www.gulp.de/gulp2/g/profil/12345"
    }

    def "buildLink for GULP prepends gulp profile URL when value does not start with http"() {
        expect:
        ContactType.GULP.buildLink("12345") == "https://www.gulp.de/gulp2/g/profil/12345"
    }

    def "buildLink for FAX returns tel link"() {
        expect:
        ContactType.FAX.buildLink("+4930123456") == "tel:+4930123456"
    }
}
