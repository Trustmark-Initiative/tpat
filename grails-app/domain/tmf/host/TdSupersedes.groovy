package tmf.host
/**
 * Holds a record of supersedes information when a newer trustmark definition supersedes an older one.
 */
class TdSupersedes {

    /**
     * The newer trustmark, which should be considered more up-to-date.
     */
    TrustmarkDefinition superseder
    /**
     * The older trustmark, which is more out-of-date.
     */
    TrustmarkDefinition superseded

    static constraints = {
        superseder(nullable: false)
        superseded(nullable: false)
    }

    static mapping = {
        table 'trustmark_definition_supersedes'
        superseder(column: 'superseder_td_ref')
        superseded(column: 'superseded_td_ref')
    }

}