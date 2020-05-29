package tmf.host

/**
 * Represents ALL content in a VersionSet at a particular point in time.
 */
class VersionSetAll {

    static belongsTo = [versionSet: VersionSet]
    BinaryObject zipFile
    Date dateCreated

    static constraints = {
        versionSet(nullable: false)
        zipFile(nullable: false)
        dateCreated(nullable: true)
    }

}
