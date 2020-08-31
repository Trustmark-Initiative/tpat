package tmf.host

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.TrustmarkFramework
import grails.core.GrailsApplication
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.search.FullTextSession
import org.hibernate.search.Search
import org.hibernate.search.SearchFactory
import tmf.host.util.TFAMPropertiesHolder
import tmf.host.util.TfamOwnerOrganization

import javax.servlet.ServletContext
import java.util.regex.Pattern

class BootStrap {

    GrailsApplication grailsApplication

    def assetResourceLocator

    def init = { ServletContext servletContext ->
        long start = System.currentTimeMillis()
        log.info "Application starting up..."

        String filesDirectory = grailsApplication.config.tf.files.directory
        filesDirectory = replaceWebapp(filesDirectory, servletContext)
        log.info("Files Directory: @|green ${filesDirectory}|@")
        SystemVariable.storeProperty(SystemVariable.FILES_DIRECTORY, filesDirectory)

        checkSecurityInit()

        log.info("Rebuilding search index...")
        VersionSetTDLink.withTransaction {
            SessionFactory sessionFactory = grailsApplication.mainContext.sessionFactory
            Session session = sessionFactory.currentSession

            FullTextSession fullTextSession = Search.getFullTextSession(session)
            SearchFactory searchFactory = fullTextSession.getSearchFactory()
            log.warn("search factory has ${searchFactory.indexedTypes?.size()} indexed types.")
            for (Class c : searchFactory.getIndexedTypes()) {
                log.warn("Found indexed type: " + c)
            }

            log.debug("Building TD index...")
            VersionSetTDLink.search().createIndexAndWait()
            log.debug("Building TIP index...")
            VersionSetTIPLink.search().createIndexAndWait()
        }

        String info = buildStartupInfo()
        addDefaultProperties()

        long stop = System.currentTimeMillis()
        log.info("Startup @|green Successful|@ in @|cyan ${(stop - start)}|@ms: \n" + info)
    }
    def destroy = {
    }


    private String buildStartupInfo() {
        StringBuilder info = new StringBuilder()

        def tmf = FactoryLoader.getInstance(TrustmarkFramework.class)

        info.append("    --------------------------------------------------------------------------------------------------------------------\n")
        info.append("    | GTRI Trust Policy Authoring Tool, v.${grailsApplication.metadata.getApplicationVersion()}\n")
        info.append("    |     TMF API Version: ${tmf.getApiVersion()}, Build Date: ${tmf.getApiBuildDate()}\n")
        info.append("    |     TMF API Impl Version: ${tmf.getApiImplVersion()}, Build Date: ${tmf.getApiImplBuildDate()}\n")
        info.append("    |     TMF Supported Version: @|cyan ${tmf.getTrustmarkFrameworkVersion()}|@\n")
        info.append("    | \n")
        info.append("    |   Configuration Information (@see /WEB-INF/classes/tpat_config.properties): \n")
        info.append("    |     Files Directory: @|green ${TFAMPropertiesHolder.getFilesDirectory()}|@\n")
        info.append("    |     Base URLs(@|cyan ${TFAMPropertiesHolder.getBaseURLs().size()}|@):\n")
        for (String url : TFAMPropertiesHolder.getBaseURLsAsStrings()) {
            info.append("    |         [${url}]\n")
        }
        info.append("    |     Registry URLs(@|cyan ${TFAMPropertiesHolder.getRegistryUrls().size()}|@):\n")
        for (String url : TFAMPropertiesHolder.getRegistryUrlsAsStrings()) {
            info.append("    |         [${url}]\n")
        }
        info.append("    |     Organization Information: \n")
        TfamOwnerOrganization ownerOrganization = TFAMPropertiesHolder.getDefaultEntity()
        info.append("    |         URI:  [@|cyan ${ownerOrganization.getIdentifier()?.toString()}|@]\n")
        info.append("    |         Name: [@|green ${ownerOrganization.getName()?.toString()}|@]\n")
        info.append("    |         Abbr: [@|green ${ownerOrganization.getAbbreviation()?.toString()}|@]\n")
        info.append("    |         Logo: [@|green ${ownerOrganization.getLogoImagePath()?.toString()}|@]\n")
        info.append("    |         Contact Email: [@|yellow ${ownerOrganization.getDefaultContact()?.getDefaultEmail()}|@]\n")
        info.append("    --------------------------------------------------------------------------------------------------------------------\n\n")
        return info.toString()
    }

    private void checkSecurityInit() {
        Role.withTransaction {
            log.debug "Checking security..."
            List<Role> roles = Role.findAll()
            if (roles.size() == 0) {
                log.info "Creating security roles..."
                log.debug("Adding role[@|cyan ${Role.ROLE_ADMIN}|@]...")
                new Role(authority: Role.ROLE_ADMIN).save(failOnError: true)
                log.debug("Adding role[@|cyan ${Role.ROLE_ORG_ADMIN}|@]...")
                new Role(authority: Role.ROLE_ORG_ADMIN).save(failOnError: true)
                log.debug("Adding role[@|cyan ${Role.ROLE_DEVELOPER}|@]...")
                new Role(authority: Role.ROLE_DEVELOPER).save(failOnError: true)
                log.debug("Adding role[@|cyan ${Role.ROLE_REVIEWER}|@]...")
                new Role(authority: Role.ROLE_REVIEWER).save(failOnError: true)
            } else {
                log.debug "Successfully found @|green ${roles.size()}|@ roles."
            }
        }

        if (User.count() == 0) {
            log.info "Creating default users..."
            User.withTransaction {
                createSingleUser()
            }
        } else {
            log.debug("Found @|green ${User.count()}|@ users in the database already.")
        }
    }


    private void createSingleUser() {
            User user = new User(
                    username: grailsApplication.config.tf.org.user,
                    password: grailsApplication.config.tf.org.pswd,
                    name: grailsApplication.config.tf.org.username,
                    accountExpired: false,
                    accountLocked: false,
                    passwordExpired: false
            )
            user.save(failOnError: true)

            String rolesForThisUser = "ROLE_ADMIN, ROLE_ORG_ADMIN, ROLE_DEVELOPER, ROLE_REVIEWER"

            for (String roleName : rolesForThisUser.split(Pattern.quote(","))) {
                roleName = roleName.trim()
                Role role = Role.findByAuthority(roleName)
                UserRole.create(user, role, true)
            }

            log.debug "Successfully created user: @|cyan " + user.name + "|@ <@|magenta " + user.username + "|@>"
    }

    private void createDefaultUsers() {
        ResourceBundle defaultUserBundle = ResourceBundle.getBundle("defaultAccounts")
        if (defaultUserBundle == null)
            throw new UnsupportedOperationException("Cannot read users from defaultAccounts.properties")

        Integer userCount = Integer.parseInt(defaultUserBundle.getString("user.count"))
        for (int i = 0; i < userCount; i++) {
            int userKey = i + 1

            User user = new User(
                    username: defaultUserBundle.getString("user." + userKey + ".username"),
                    password: defaultUserBundle.getString("user." + userKey + ".password"),
                    name: defaultUserBundle.getString("user." + userKey + ".name"),
                    accountExpired: false,
                    accountLocked: false,
                    passwordExpired: false
            )
            user.save(failOnError: true)

            String rolesForThisUser = defaultUserBundle.getString("user." + userKey + ".roles")
            for (String roleName : rolesForThisUser.split(Pattern.quote(","))) {
                roleName = roleName.trim()
                Role role = Role.findByAuthority(roleName)
                UserRole.create(user, role, true)
            }
            log.debug "Successfully created user: @|cyan " + user.name + "|@ <@|magenta " + user.username + "|@>"
        }

    }

    private void addDefaultProperties()  {
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_TDO_ID, "Default Trustmark Defining Organization ID", "Contains the default value for your Trustmark Defining Organization Identifier.", grailsApplication.config.tdo.identifier)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_TDO_NAME, "Default Trustmark Defining Organization Name", "Contains the default value for your Trustmark Defining Organization Name.", grailsApplication.config.tdo.name)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_TDO_PRIMARY_POC_EMAIL, "Default Trustmark Defining Organization Primary POC Email", "Contains the default value for the email address for your primary point of contact.", grailsApplication.config.tdo.primaryContact.email)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_TDO_PRIMARY_POC_PHONE, "Default Trustmark Defining Organization Primary POC Telephone Number", "Contains the default value for the telephone number for your primary point of contact.", grailsApplication.config.tdo.primaryContact.telephone)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_TDO_PRIMARY_POC_ADDRESS, "Default Trustmark Defining Organization Primary POC Mailing Address", "Contains the default value for the physical mailing address for your primary point of contact.", grailsApplication.config.tdo.primaryContact.mailingAddress)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_ISSUANCE_CRITERIA, "Default Issuance Criteria", "Contains the default value for the Boolean expression that indicates whether a trustmark provider may issue a trustmark to a trustmark recipient, based on the results of a formal assessment process using the trustmark definition's assessment steps.", grailsApplication.config.defaultIssuanceCriteria)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_TARGET_STAKEHOLDER_DESC, "Default Target Stakeholder Description", "Contains the default value for the intended communities and stakeholder groups to which your published trustmark definitions apply.", grailsApplication.config.targetStakeHolderDescription)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_TARGET_RECIPIENT_DESC, "Default Target Recipient Description", "Contains the default value for the intended organizations to which trustmarks would be issued under your published trustmark definitions.", grailsApplication.config.targetRecipientDescription)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_TARGET_RELYING_PARTY_DESC, "Default Target Relying Party Description", "Contains the default value for the intended trustmark relying parties for trustmarks issued under your published trustmark definitions.", grailsApplication.config.targetRelyingPartyDescription)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_TARGET_PROVIDER_DESC, "Default Target Provider Description", "Contains the default value for the intended organizations that would act as trustmark providers and issue trustmarks under your published trustmark definitions.", grailsApplication.config.targetProviderDescription)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_PROVIDER_ELIGIBILITY_CRITERIA, "Default Provider Eligibility Criteria", "Contains the default value for the criteria that an organization must meet to become eligible to act as a trustmark provider and issue trustmarks under your published trustmark definitions.", grailsApplication.config.providerEligibilityCriteria)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_ASSESSOR_QUALIFICATIONS_DESC, "Default Assessor Qualifications Description", "Contains the default value for the qualifications that an individual must possess to act as an assessor on behalf of a trustmark provider that issues trustmarks under your published trustmark definitions.", grailsApplication.config.assessorQualificationsDescription)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_TM_REVOCATION_CRITERIA, "Default Trustmark Revocation Criteria", "Contains the default value for the criteria that, if triggered, would require that the trustmark provider revoke a trustmark issued under your published trustmark definitions. Note that, if this field is not specified, then by default the trustmark provider must revoke any trustmark issued under the trustmark definition upon discovery of any information indicating that the trustmark recipient no longer fulfills one or more of the conformance criteria in the trustmark definition.", grailsApplication.config.trustmarkRevocationCriteria)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_EXTENSION_DESC, "Default Extension Description", "Contains the default value for the normative requirements for populating the \"Extension\" element of a trustmark issued under your published trustmark definitions.", grailsApplication.config.extensionDescription)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_TD_LEGAL_NOTICE, "Default Legal Notice", "Contains the default value for the legal notice for your published trustmark definitions.", grailsApplication.config.defaultTdLegalNotice)
        DefaultVariable.storeTdProperty(DefaultVariable.DEFAULT_TD_NOTES, "Default Notes", "Contains the default value for additional optional text content about your published trustmark definitions.", grailsApplication.config.defaultTdNotes)

        DefaultVariable.storeTipProperty(DefaultVariable.DEFAULT_ISSUER_ID, "Default Issuer ID", "Contains the default value for your Trust Interoperability Profile Issuer Identifier.", grailsApplication.config.tipIssuer.identifier)
        DefaultVariable.storeTipProperty(DefaultVariable.DEFAULT_ISSUER_NAME, "Default Issuer Name", "Contains the default value for your Trust Interoperability Profile Issuer Name.", grailsApplication.config.tipIssuer.name)
        DefaultVariable.storeTipProperty(DefaultVariable.DEFAULT_ISSUER_PRIMARY_POC_EMAIL, "Default Issuer Primary POC Email", "Contains the default value for the email address for your primary point of contact.", grailsApplication.config.tipIssuer.primaryContact.email)
        DefaultVariable.storeTipProperty(DefaultVariable.DEFAULT_ISSUER_PRIMARY_POC_PHONE, "Default Issuer Primary POC Telephone Number", "Contains the default value for the telephone number for your primary point of contact.", grailsApplication.config.tipIssuer.primaryContact.telephone)
        DefaultVariable.storeTipProperty(DefaultVariable.DEFAULT_ISSUER_PRIMARY_POC_ADDRESS, "Default Issuer Primary POC Mailing Address", "Contains the default value for the physical mailing address for your primary point of contact.", grailsApplication.config.tipIssuer.primaryContact.mailingAddress)
        DefaultVariable.storeTipProperty(DefaultVariable.DEFAULT_NOTES, "Default Notes", "Contains the default value for additional optional text content about your published trust interoperability profiles.", grailsApplication.config.defaultNotes)
        DefaultVariable.storeTipProperty(DefaultVariable.DEFAULT_LEGAL_NOTICE, "Default Legal Notice", "Contains the default value for the legal notice for your published trust interoperability profiles.", grailsApplication.config.defaultLegalNotice)
        DefaultVariable.storeTipProperty(DefaultVariable.DEFAULT_TRUSTMARK_PROVIDERS, "Default Trustmark Providers", "Contains a list of approved trustmark providers for all trustmarks referenced by your trust interoperability profiles. Each provider is separated by a pipe ( | ) character", grailsApplication.config.tdProviderReferenceIds)
    }

    private String replaceWebapp(String str, ServletContext context) {
        String realPath = context.getRealPath("/")
        if (realPath.endsWith(File.separator))
            realPath = realPath.substring(0, realPath.length() - 1)
        return str?.replace("webapp:", realPath)
    }
}
