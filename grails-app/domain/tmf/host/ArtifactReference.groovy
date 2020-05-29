package tmf.host

/**
 * A table for mapping TIP References.  This allows us to calculate operations such as "What contains this?" easily.
 * <br/><br/>
 * @author brad
 * @date 5/25/17
 */
class ArtifactReference {

    /**
     * The version set containing this reference.
     */
    static belongsTo = [versionSet: VersionSet]

    /**
     * The source TIP (ie, containing this reference).
     */
    TrustInteroperabilityProfile sourceTip;

    /**
     * If not null, then this is a TIP to TIP reference.  Cannot have a value if destinationTd or externalReference has a value.
     */
    TrustInteroperabilityProfile destinationTip;

    /**
     * If not null, then this is a TIP to TD reference.  Cannot have a value if destinationTip or externalReference has a value.
     */
    TrustmarkDefinition destinationTd;

    /**
     * If this is an externally reference thing, then the value will be here. Cannot have a value if destinationTip or destinationTd has a value.
     */
    ExternalReference externalReference;



    static constraints = {
        versionSet(nullable: false)
        sourceTip(nullable: false)
        destinationTd(nullable: true)
        destinationTip(nullable: true)
        externalReference(nullable: true)
    }

    static mapping = {
        versionSet(column: "version_set_ref")
        sourceTip(column: "source_tip_ref")
        destinationTd(column: "dest_td_ref")
        destinationTip(column: "dest_tip_ref")
        externalReference(column: "external_ref")
    }

}/* end ArtifactReference */