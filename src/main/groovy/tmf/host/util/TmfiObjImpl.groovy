package tmf.host.util

import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkFrameworkIdentifiedObject

/**
 * Created by brad on 8/4/17.
 */
class TmfiObjImpl implements TrustmarkFrameworkIdentifiedObject {

    String typeName
    URI identifier
    String name
    Integer number
    String version
    String description

    String getTypeName() {
        return typeName
    }

    void setTypeName(String typeName) {
        this.typeName = typeName
    }

    URI getIdentifier() {
        return identifier
    }

    void setIdentifier(URI identifier) {
        this.identifier = identifier
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    Integer getNumber() {
        return number
    }

    void setNumber(Integer number) {
        this.number = number
    }

    String getVersion() {
        return version
    }

    void setVersion(String version) {
        this.version = version
    }

    String getDescription() {
        return description
    }

    void setDescription(String description) {
        this.description = description
    }
}
