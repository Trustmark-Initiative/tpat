package tmf.host

/**
 * TODO: Insert Comment Here
 * <br/><br/>
 * @author brad
 * @date 5/24/17
 */
class TipTreeCache {

    static belongsTo = [tip: TrustInteroperabilityProfile]
    BinaryObject binaryObject
    Date dateCreated

    static constraints = {
        tip(nullable: false)
        binaryObject(nullable: false)
        dateCreated(nullable: true)
    }

    static mapping = {
        tip(column: 'tip_ref')
        binaryObject(column: 'binary_object_ref')
    }

}
