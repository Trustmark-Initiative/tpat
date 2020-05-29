package tmf.host

/**
 * Stores the event when a user uploads a file to a version set.
 * <br/><br/>
 * @user brad
 * @date 11/30/16
 */
class VersionSetUpload implements Comparable<VersionSetUpload>{

    static belongsTo = [versionSet: VersionSet]

    /**
     * Who uploaded this file.
     */
    User uploadedBy

    /**
     * When the upload occurred.
     */
    Date dateCreated

    /**
     * The actual artifact uploaded.
     */
    BinaryObject artifact

    /**
     * Whether or not this file has been applied to do the version set yet.
     */
    Boolean processed = false;

    static constraints = {
        versionSet(nullable: false)
        uploadedBy(nullable: false)
        artifact(nullable: false)
        processed(nullable: false)

        dateCreated(nullable: true)
    }

    static mapping = {
        versionSet(column: 'version_set_ref')
        uploadedBy(column: 'uploaded_by_ref')
        artifact(column: 'artifact_ref')
    }


    public String toString(){
        return "VersionSetUpload[vs=${versionSet?.id}, file=${artifact?.originalFilename}]"
    }

    @Override
    int compareTo(VersionSetUpload that) {
        if( that != null && that.artifact != null )
            return this.artifact?.originalFilename.compareToIgnoreCase(that.artifact.originalFilename);

        return -1
    }
}
