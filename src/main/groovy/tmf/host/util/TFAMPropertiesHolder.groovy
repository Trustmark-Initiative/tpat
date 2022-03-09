package tmf.host.util

import edu.gatech.gtri.trustmark.v1_0.model.Contact
import edu.gatech.gtri.trustmark.v1_0.model.ContactKindCode
import edu.gatech.gtri.trustmark.v1_0.model.Entity
import grails.util.Environment
import org.apache.commons.lang.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import tmf.host.DefaultVariable
import tmf.host.Provider

import javax.servlet.ServletException
import java.util.regex.Pattern

/**
 * Provides convenient access to the TFAM Properties as a static thing.
 * <br/><br/>
 * @user brad
 * @date 12/19/16
 */
class TFAMPropertiesHolder {

    private static final Logger log = LoggerFactory.getLogger(TFAMPropertiesHolder.class);

    public static final String BUNDLE_NAME = "/tpat_config.properties"
    public static final String HEADER_COMMENTS = "# This value should be the basis for the URL you are posting this war file at (ie, everything up to and including the context path)\n" +
            "#  It is used to determine if TF artifacts are actually being hosted by this instance.  For multiple, please pipe (|)\n" +
            "#  separate them.  Note that if users attempt to edit Trustmark Definitions and set ids that don't start with one of\n" +
            "#  these values, then their edits will be rejected.\n" +
            "#  The first value in the list will be used as the default base URI during bulk creation of artifacts."

    private static Properties RESOURCES = new XProperties();
    static {
        try {
            log.info("Initializing TPAT Properties Holder...")
            ClassPathResource classPathResource = new ClassPathResource(BUNDLE_NAME)
            loadResource(classPathResource)

        } catch(Throwable t) {
            log.error("Error reading TPAT Config properties!", t)
            throw new RuntimeException("Cannot load TPAT Config properties: "+BUNDLE_NAME, t)
        }
    }

    static ResourceBundle getBundle(){return null}

    static Properties getProperties(){return RESOURCES}
    //==================================================================================================================
    //  Data specific methods.
    //==================================================================================================================
    private static TfamOwnerOrganization defaultTfamOwnerOrganization
    private static List<URL> baseUrls = null
    private static List<String> baseUrlsAsStrings = null
    private static List<URL> registryUrls = null
    private static List<String> registryUrlsAsStrings = null
    private static List<String> keywordsToIgnore = null
    private static List<URL> providerReferences = null

    static List<String> getKeywordsToIgnore() {
        synchronized (log){
            if( keywordsToIgnore )
                return keywordsToIgnore
            keywordsToIgnore = getStringList("keywords.to.ignore") ?: []
            return keywordsToIgnore
        }
    }

    static String getFilesDirectory(){
        return getString("tf.files.directory", "/tmp/tfam")
    }

    static QuartzConfig getQuartzConfig(String name){
        return new QuartzConfig(getProperties(), name)
    }

    /**
     * Returns the TfamOwnerOrganization entity from DB {@link Entity}
     * <br/><br/>
     * @return
     */
    static TfamOwnerOrganization getDefaultEntity() {
        Provider.withTransaction {
            List<Provider> providers = Provider.findAll()
            if (providers.isEmpty()){
                log.error("No providers are available in the database")
                return null
            }
            for (Provider p : providers) {
                log.debug("Processing Provider: " + p)
                if (p.getTd()) {
                    TfamOwnerOrganization ownerOrganization = new TfamOwnerOrganization()
                    ownerOrganization.setIdentifier(new URI(p.getUri()))
                    ownerOrganization.setName(p.getName())
                    final List<Contact> contacts = []
                    Contact c = new ContactImpl()
                    c.kind = ContactKindCode.PRIMARY
                    if(StringUtils.isNotBlank(p.getEmail()))
                        c.emails.add(p.getEmail())
                    if(StringUtils.isNotBlank(p.getTelephone()))
                        c.telephones.add(p.getTelephone())
                    if(StringUtils.isNotBlank(p.getMailingAddress()))
                        c.mailingAddresses.add(p.getMailingAddress())
                    if(StringUtils.isNotBlank(p.getResponder()))
                        c.responder = p.responder
                    if(StringUtils.isNotBlank(p.getUri()))
                        c.websiteURLs.add(new URL(p.getUri()))
                    if(StringUtils.isNotBlank(p.getNotes()))
                        c.notes = p.getNotes()
                    contacts.add(c)
                    ownerOrganization.setContacts(contacts)
                    return ownerOrganization
                }
            }
        }
    }

    /**
     * Returns the TfamOwnerOrganization entity configured {@link Entity} for this TFAM from properties file
     * <br/><br/>
     * @return
     */
    static TfamOwnerOrganization getDefaultEntityFromProperties() {
        synchronized (log){
            if( defaultTfamOwnerOrganization )
                return defaultTfamOwnerOrganization

            final String fName = getString("org.name", "<DEFAULT ORG NAME NOT SET>")
            final String fId = getString("org.identifier", "urn:error:missing")
            final String fAbbr = getString("org.abbreviation", "")
            final String fLogoPath = getString("org.logo.imagepath", "logo.png")

            final List<Contact> contacts = []
            if( getNumber("org.contact.count") != null ){
                for( int i = 1; i <= getNumber("org.contact.count").intValue(); i++ ){
                    Contact c = new ContactImpl()
                    if( exists("org.contact.${i}.kind") ){
                        if( getString("org.contact.${i}.kind").equalsIgnoreCase("primary") ){
                            c.kind = ContactKindCode.PRIMARY
                        }else{
                            c.kind = ContactKindCode.OTHER
                        }
                    }

                    if( exists("org.contact.${i}.responder") )
                        c.responder = getString("org.contact.${i}.responder")

                    c.emails.add(getString("org.contact.${i}.email", "<EMAIL_NOT_GIVEN>@<domain>"))

                    if( exists("org.contact.${i}.phone") )
                        c.telephones.add(getString("org.contact.${i}.phone"))

                    if( exists("org.contact.${i}.mailingAddress") )
                        c.mailingAddresses.add(getString("org.contact.${i}.mailingAddress"))

                    if( exists("org.contact.${i}.physicalAddress") )
                        c.physicalAddresses.add(getString("org.contact.${i}.physicalAddress"))

                    if( exists("org.contact.${i}.websiteURL") )
                        c.websiteURLs.add(new URL(getString("org.contact.${i}.websiteURL")))

                    if( exists("org.contact.${i}.notes") )
                        c.notes = new URL(getString("org.contact.${i}.notes"))

                    contacts.add(c)
                }
            }

            TfamOwnerOrganization ownerOrganization = new TfamOwnerOrganization()
            ownerOrganization.setIdentifier(new URI(fId))
            ownerOrganization.setName(fName)
            ownerOrganization.setAbbreviation(fAbbr)
            ownerOrganization.setLogoImagePath(fLogoPath)
            ownerOrganization.setContacts(contacts)

            defaultTfamOwnerOrganization = ownerOrganization
            log.debug("Returning defaultTfamOwnerOrganization" + defaultTfamOwnerOrganization)
            return defaultTfamOwnerOrganization
        }
    }

    /**
     * Returns the list of configured registry URL objects.
     */
    static List<URL> getRegistryUrls(){
        synchronized (log){
            if( registryUrls )
                return registryUrls
            registryUrls = getUrlList("registry.urls")
            return registryUrls
        }
    }

    /**
     * Returns the list of configured registry URL objects as Strings.
     */
    static List<String> getRegistryUrlsAsStrings(){
        synchronized (log){
            if( registryUrlsAsStrings )
                return registryUrlsAsStrings
            registryUrlsAsStrings = getStringList("registry.urls")
            return registryUrlsAsStrings
        }
    }

    /**
     * Returns the first configured base URL object.
     */
    static URL getBaseURL() {
        synchronized (log) {
            return getBaseURLs().get(0)
        }
    }

    /**
     * Returns the list of configured base URL objects.
     */
    static List<URL> getBaseURLs(){
        synchronized (log){
            if( baseUrls )
                return baseUrls
            baseUrls = getUrlList("tf.base.url")
            return baseUrls
        }
    }

    /**
     * Returns the first configured base URL object.
     */
    static String getBaseUrlAsString() {
        synchronized (log) {
            return getBaseURLsAsStrings().get(0)
        }
    }

    /**
     * Returns the list of configured base URL objects.
     */
    static List<String> getBaseURLsAsStrings(){
        synchronized (log){
            if( baseUrlsAsStrings )
                return baseUrlsAsStrings
            baseUrlsAsStrings = getStringList("tf.base.url")
            return baseUrlsAsStrings
        }
    }


    /**
     * Returns a list of URL objects which can be used as "provider references" in TIP references of TDs.
     */
    static List<URL> getProviderReferences(){
        //instead of retrieving the list from properties through getUrlList("-tdProviderReferenceIds") retrieve from db
        List urlsList = []
        log.warn("Generating list of ProviderReferences");
        Provider.withTransaction {
            for (Provider p : Provider.findAll()) {
                log.debug("Processing Provider: " + p)
                if(p.getTp())
                    urlsList.add(new URL(p.getUri()))
            }
        }
        log.warn("Generated list of ProviderReferences...${urlsList}");
        return urlsList
    }

    static String getTdIdentifierUriBase(){
        // This must match the URL Mappings.
        return getBaseUrlAsString() + "/tds"; 
    }

    static String getTipIdentifierUriBase(){
        // This must match the URL Mappings.
        return getBaseUrlAsString() + "/tips";
    }

    static String getDefaultVersion(){
        //TODO review if we want to propagate defaultVersion from config file.
        return getString("defaultVersion")
    }

    static String getDefaultTdLegalNotice(){
        return DefaultVariable.getPropertyValue(DefaultVariable.DEFAULT_TD_LEGAL_NOTICE)
    }

    static String getDefaultTdNotes(){
        return DefaultVariable.getPropertyValue(DefaultVariable.DEFAULT_TD_NOTES)
    }

    static String getDefaultTipLegalNotice(){
        return DefaultVariable.getPropertyValue(DefaultVariable.DEFAULT_TIP_LEGAL_NOTICE)
    }

    static String getDefaultTipNotes(){
        return DefaultVariable.getPropertyValue(DefaultVariable.DEFAULT_TIP_NOTES)
    }

    static String getDefaultIssuanceCriteria(){
        return DefaultVariable.getPropertyValue(DefaultVariable.DEFAULT_ISSUANCE_CRITERIA)
    }

    static String getDefaultRevocationCriteria(){
        return DefaultVariable.getPropertyValue(DefaultVariable.DEFAULT_TM_REVOCATION_CRITERIA)
    }

    static    String getDefaultTargetStakeholderDescription() {
        return DefaultVariable.getPropertyValue(DefaultVariable.DEFAULT_TARGET_STAKEHOLDER_DESC)
    }

    static     String getDefaultTargetRecipientDescription() {
        return DefaultVariable.getPropertyValue(DefaultVariable.DEFAULT_TARGET_RECIPIENT_DESC)
    }

    static     String getDefaultTargetRelyingPartyDescription() {
        return DefaultVariable.getPropertyValue(DefaultVariable.DEFAULT_TARGET_RELYING_PARTY_DESC)
    }

    static     String getDefaultTargetProviderDescription() {
        return DefaultVariable.getPropertyValue(DefaultVariable.DEFAULT_TARGET_PROVIDER_DESC)
    }

    static     String getDefaultProviderEligibilityCriteria() {
        return DefaultVariable.getPropertyValue(DefaultVariable.DEFAULT_PROVIDER_ELIGIBILITY_CRITERIA)
    }

    static     String getDefaultAssessorQualificationsDescription() {
        return DefaultVariable.getPropertyValue(DefaultVariable.DEFAULT_ASSESSOR_QUALIFICATIONS_DESC)
    }

    static    String getDefaultExtensionDescription() {
        return DefaultVariable.getPropertyValue(DefaultVariable.DEFAULT_EXTENSION_DESC)
    }

    static void storeProperties()
    {
        RESOURCES.store(new FileWriter(BUNDLE_NAME), HEADER_COMMENTS)
    }
    static void storeProperties(String filename)
    {
        if(filename == null) {
            storeProperties()
            return
        }
        RESOURCES.store(new FileWriter(filename), HEADER_COMMENTS)
    }

    //==================================================================================================================
    //  Generalized Data Methods
    //==================================================================================================================
    private static void loadResource(ClassPathResource classPathResource) throws Throwable {
        InputStream inputStream = classPathResource.getInputStream()
        String propertiesTxt = inputStream.text
        if( log.isDebugEnabled() )
            log.debug("### OUTPUT OF ${classPathResource.path} : \n"+propertiesTxt+"\n### END OF ${classPathResource.path}")
        RESOURCES.load(new StringReader(propertiesTxt))
    }

    private static boolean exists(String property){
        if( getProperties() ){
            return getProperties().containsKey(property)
        }else{
            return false
        }
    }

    private static List<URL> getUrlList(String property){
        if( exists(property) ){
            List stringsList = getStringList(property)
            List urlsList = []
            for( String urlString : stringsList ){
                urlsList.add(new URL(urlString))
            }
            return urlsList
        }else{
            return []
        }
    }

    private static List<String> getStringList(String property){
        if( exists(property) ){
            List theReturnList = []
            String theStringList = getString(property)
            String[] stringLines = theStringList.split(Pattern.quote("|"))
            if( stringLines != null && stringLines.length > 0 ) {
                for (String aStringValue : stringLines) {
                    theReturnList.add(aStringValue.trim())
                }
            }
            return theReturnList
        }else{
            return []
        }
    }

    private static String getString(String property){
        return getString(property, null)
    }
    private static String getString(String property, String defaultValue){
        Properties props = getProperties()
        if( props != null ){
            try{
                String val = props.getProperty(property)
                if( val != null && val.trim().length() > 0 ){
                    return val.trim();
                }else{
                    return defaultValue
                }
            }catch(Throwable t){
                return defaultValue
            }
        }else{
            return defaultValue
        }
    }

    private static Number getNumber(String property){
        return getNumber(property, null)
    }

    private static Number getNumber(String property, Number defaultValue){
        String value = getString(property, null)
        if( value ){
            try{
                return Double.parseDouble(value)
            }catch(Throwable T){
                return defaultValue
            }
        }else{
            return defaultValue
        }
    }

    private static boolean getBoolean(String property){
        return getBoolean(property, null)
    }
    private static boolean getBoolean(String property, Number defaultValue){
        String value = getString(property, null)
        if( value ){
            try{
                return Boolean.parseBoolean(value)
            }catch(Throwable T){
                return defaultValue
            }
        }else{
            return defaultValue
        }
    }


}
