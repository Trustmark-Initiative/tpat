package tmf.host
/**
 * Represents a link from a {@link TrustmarkDefinition} to a {@link Keyword}, in the context of a particular {@link VersionSet}.
 */
class KeywordTDLink {

    static belongsTo = [versionSet: VersionSet]
    Keyword keyword

    TrustmarkDefinition td

    static constraints = {
        versionSet(nullable: false)
        keyword(nullable: false)
        td(nullable: false)
    }

    static mapping = {
        table 'keyword_td_link'
        keyword(column: 'keyword_ref')
        td(column: 'trustmark_definition_ref')
        versionSet(column: 'version_set_ref')
    }

}