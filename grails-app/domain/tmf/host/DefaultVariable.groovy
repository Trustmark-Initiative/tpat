package tmf.host

class DefaultVariable {

    public static final String DEFAULT_NOTES = "DEFAULT_NOTES"
    public static final String DEFAULT_LEGAL_NOTICE = "DEFAULT_LEGAL_NOTICE"

    public static final String DEFAULT_ISSUANCE_CRITERIA = "DEFAULT_ISSUANCE_CRITERIA"
    public static final String DEFAULT_TARGET_STAKEHOLDER_DESC = "DEFAULT_TARGET_STAKEHOLDER_DESC"
    public static final String DEFAULT_TARGET_RECIPIENT_DESC = "DEFAULT_TARGET_RECIPIENT_DESC"
    public static final String DEFAULT_TARGET_RELYING_PARTY_DESC = "DEFAULT_TARGET_RELYING_PARTY_DESC"
    public static final String DEFAULT_TARGET_PROVIDER_DESC = "DEFAULT_TARGET_PROVIDER_DESC"
    public static final String DEFAULT_PROVIDER_ELIGIBILITY_CRITERIA = "DEFAULT_PROVIDER_ELIGIBILITY_CRITERIA"
    public static final String DEFAULT_ASSESSOR_QUALIFICATIONS_DESC = "DEFAULT_ASSESSOR_QUALIFICATIONS_DESC"
    public static final String DEFAULT_TM_REVOCATION_CRITERIA = "DEFAULT_TM_REVOCATION_CRITERIA"
    public static final String DEFAULT_EXTENSION_DESC = "DEFAULT_EXTENSION_DESC"
    public static final String DEFAULT_TD_NOTES = "DEFAULT_TD_NOTES"
    public static final String DEFAULT_TD_LEGAL_NOTICE = "DEFAULT_TD_LEGAL_NOTICE"

    String name
    String title
    boolean tdRelated
    boolean tipRelated
    String fieldValue
    String description
    Date lastUpdated

    static void storeProperty(String name, String titl, String desc, Object value, boolean tdRel, boolean tipRel) {
        if(value == null)  {
            value = ""
        }

        DefaultVariable.withTransaction {
            DefaultVariable prop = DefaultVariable.findByName(name)
            if( !prop ) {
                prop = new DefaultVariable(name: name, title: titl, description: desc, fieldValue: value?.toString(), tdRelated: tdRel, tipRelated: tipRel)
                prop.save(failOnError: true)
                log.debug("Property added to DB: ${prop.title} ")
            } else {
                log.debug("Property already exists: ${prop.title} ")
//                prop.fieldValue = value?.toString()
            }
            //prop.save(failOnError: true)
        }
    }

    static String getPropertyValue(String name) {
        DefaultVariable.withTransaction {
            DefaultVariable prop = DefaultVariable.findByName(name)
            if (!prop){
                log.error("Invalid property name specified: ${name}")
                return ""
            }
            return prop.fieldValue;
        }
    }

    static void loadProperty(String name) {
        DefaultVariable.withTransaction {
            DefaultVariable prop = DefaultVariable.findByName(name)
            if (!prop){
                log.error("Invalid property name specified: ${name}")
                return ""
            }
            return prop
        }
    }

    static void deleteProperty(String name) {
        DefaultVariable.withTransaction {
            DefaultVariable prop = DefaultVariable.findByName(name)
            if( prop )
                prop.delete()
        }
    }

    static void storeTdProperty(String name, Object value, String title, String desc) {
        storeProperty(name, title, desc, value, true, false);
    }

    static void storeTipProperty(String name, Object value, String title, String desc) {
        storeProperty(name, title, desc, value, false, true);
    }

    static constraints = {
        name(nullable: false, blank: false, maxSize: 254)
        fieldValue(nullable: true, blank: true, maxSize: 65535)
        title(nullable: false, blank: false, maxSize: 65535)
        description(nullable: false, blank: false, maxSize: 65535)
        lastUpdated(nullable: true)
        tdRelated(nullable: false)
        tipRelated(nullable: false)
        lastUpdated(nullable: true)
    }

    static mapping = {
        table 'default_variable'
        name column: 'field_name'
        tdRelated type: 'boolean', column: 'td_related'
        tipRelated type: 'boolean', column: 'tip_related'
        title type: 'text', column: 'title'
        description type: 'text', column: 'description'
        fieldValue type: 'text', column: 'field_value'
        lastUpdated column: 'last_updated'
    }

    static transients = ['booleanValue', 'stringValue', 'numericValue', 'value']


}
