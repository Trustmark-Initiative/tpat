package tmf.host
/**
 * Represents a keyword as defined in a tmf.host.TrustmarkDefinition
 */
class Keyword {

    String name

    static constraints = {
        name(nullable: false, blank: false, maxSize: 128, unique: true)
    }

    static mapping = {
        table 'keyword'
        name(column: 'name')
    }
}