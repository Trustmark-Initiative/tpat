package tmf.host
/**
 * Represents a link from a {@link TrustInteroperabilityProfile} to a {@link Keyword}, in the context of a particular {@link VersionSet}.
 */
class KeywordTIPLink {

    static belongsTo = [versionSet: VersionSet]
    Keyword keyword
    TrustInteroperabilityProfile tip

    static constraints = {
        versionSet(nullable: false)
        keyword(nullable: false)
        tip(nullable: false)
    }

    static mapping = {
        table 'keyword_tip_link'
        keyword(column: 'keyword_ref')
        tip(column: 'tip_ref')
        versionSet(column: 'version_set_ref')
    }

}