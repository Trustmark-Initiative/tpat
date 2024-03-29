package tmf.host

import grails.converters.JSON

import grails.util.Environment
import org.apache.commons.lang.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.ObjectError

import javax.servlet.ServletException

/**
 * Provides operations for accessing nad modifying Provider domain objects.
 */
class ProviderController {
    //==================================================================================================================
    //  Web Methods
    //==================================================================================================================
    @PreAuthorize('hasAuthority("tpat-admin")')
    def index() {
        redirect(action: 'list')
    }//end index()

    @PreAuthorize('hasAuthority("tpat-admin")')
    def list(){
        log.info("Listing all providers...");

        params.max = Math.min(params.int("max") ?: 20, 100);

        def providers = Provider.list(params)

        withFormat {
            html {
                [providerCount: Provider.count(), providers: providers]
            }
            json {
                Map response = [
                        totalCount: Provider.count(),
                        count: providers.size(),
                        max: params.int("max") ?: 20,
                        offset: params.int("offset") ?: 0,
                        sort: params.sort ?: "id",
                        order: params.order ?: "asc",
                        providers: []
                ]

                for( Provider provider : providers ?: [] ){
                    response.providers.add(provider.toJson());
                }

                render response as JSON
            }
        }

    }//end list()

    @PreAuthorize('hasAuthority("tpat-admin")')
    def show() {
        log.info("Request to display provider[${params.id}]...")
        if( StringUtils.isBlank(params.id) )
            throw new ServletException("Missing required param: id")

        Provider p = Provider.findByName(params.id);
        if( p == null )
            throw new ServletException("Could not find Provider: "+params.id);

        withFormat {
            html {
                [provider: p]
            }
            json {
                render p.toJson() as JSON
            }
        }


    }//end show()

    @PreAuthorize('hasAuthority("tpat-admin")')
    def clearDatabase() {
        if( Environment.current == Environment.DEVELOPMENT ){
            log.warn("Removing all providers...");
            Provider.withTransaction {
                for (Provider p : Provider.findAll()) {
                    p.delete();
                }
            }
            redirect(action: 'list');
        }else{
            throw new ServletException("Cannot clear database in non-development mode.");
        }
    }

    @PreAuthorize('hasAuthority("tpat-admin")')
    def stuffDatabase() {
        if( Environment.current == Environment.DEVELOPMENT ){
            log.info("Creating test providers...")
            def personNames = ["Mark", "Justin", "Shiela", "John", "Brad", "Candace", "Mary", "Sue"]
            def personLastNames = ["Lee", "Matthews", "Roth", "Williams", "Young", "Jones", "Taylor", "Brown", "Davies", "Evans"]
            def areaCodes = ["404", "678", "770", "901"]

            Random r = new Random(System.currentTimeMillis());

            for(int i = 0; i < 100; i++ ){
                Provider p = new Provider(name: 'Test_'+i, uri: "https://test"+i+".org")

                String name = personNames.get(r.nextInt(personNames.size()));
                String surname = personLastNames.get(r.nextInt(personLastNames.size()));
                String areaCode = areaCodes.get(r.nextInt(areaCodes.size()));

                p.responder = name + " " + surname;
                p.email = name + "@test"+i+".org";
                p.telephone = areaCode + "-" + (r.nextInt(9)+1) + (r.nextInt(9)+1) + (r.nextInt(9)+1) + "-" +
                        (r.nextInt(9)+1) + (r.nextInt(9)+1) + (r.nextInt(9)+1) + (r.nextInt(9)+1);

                p.notes = "This provider was randomly created.";

                p.save(failOnError: true)
            }

            flash.message = "Successfully created 100 example providers."
            redirect(action: 'list');
        }else{
            throw new ServletException("Cannot stuff database in non-development mode.");
        }
    }


    @PreAuthorize('hasAuthority("tpat-admin")')
    def create() {
        log.info("Displaying the create new provider form...")
        [command: new CreateProviderCommand()]
    }//end create()

    @PreAuthorize('hasAuthority("tpat-admin")')
    def save(CreateProviderCommand command) {
        log.info("Processing CreateProviderCommand...")
        if( command.hasErrors() ){
            log.warn("Found errors in CreateProviderCommand...")
            for( ObjectError oe : command.errors.allErrors ){
                log.warn("   FORM ERROR -> "+message(error:oe));
            }
            return render(view: '/provider/create', model: [command: command]);
        }

        log.info("Valid CreateProviderCommand[${command.name}, ${command.uri}] given, saving to database...");

        Provider provider = new Provider();
        provider.setName(command.getName()?.trim() ?: "");
        provider.setUri(command.getUri()?.trim() ?: "");
        provider.setResponder(command.getResponder()?.trim() ?: "");
        provider.setEmail(command.getEmail()?.trim() ?: "");
        provider.setMailingAddress(command.getMailingAddress()?.trim() ?: "");
        provider.setTelephone(command.getTelephone()?.trim() ?: "");
        provider.setNotes(command.getNotes()?.trim() ?: "");
        provider.setTd(command.getTd());
        provider.setTp(command.getTp());

        provider.save(failOnError: true)

        flash.message = "Successfully saved provider "+command.name
        redirect(action: 'list')
    }//end save()

    @PreAuthorize('hasAuthority("tpat-admin")')
    def edit() {
        log.info("Request to edit provider[${params.id}]...")
        if( StringUtils.isBlank(params.id) )
            throw new ServletException("Missing required param: id")

        Provider p = Provider.findByName(params.id);
        if( p == null )
            throw new ServletException("Could not find Provider: "+params.id);

        EditProviderCommand command = new EditProviderCommand()
        command.setData(p);
        [command: command]
    }//end edit()

    @PreAuthorize('hasAuthority("tpat-admin")')
    def setTd() {
        log.info("Request to set TD provider[${params.id}]...")
        if( StringUtils.isBlank(params.id) )
            throw new ServletException("Missing required param: id")

        Provider tdProvider = Provider.findByName(params.id);
        if( tdProvider == null )
            throw new ServletException("Could not find Provider: "+params.id);

        Provider.withTransaction {
            for (Provider p : Provider.findAll()) {
                if (p.equals(tdProvider)){
                    p.setTd(true);
                    p.save(failOnError: true)
                } else {
                    p.setTd(false);
                    p.save(failOnError: true)
                }
            }
        }

        flash.message = "Successfully set " + tdProvider.name + " as TD provider"

        redirect(action: 'list')
    }//end edit()

    @PreAuthorize('hasAuthority("tpat-admin")')
    def setTp() {
        log.info("Request to set TP provider[${params.id}]...")
        if( StringUtils.isBlank(params.id) )
            throw new ServletException("Missing required param: id")

        Provider tpProvider = Provider.findByName(params.id);
        if( tpProvider == null )
            throw new ServletException("Could not find Provider: "+params.id);

        Provider.withTransaction {
            if (tpProvider.getTp()){
                tpProvider.setTp(false);
                flash.message = "Successfully unset " + tpProvider.name + " as TP provider"
            } else {
                tpProvider.setTp(true);
                flash.message = "Successfully set " + tpProvider.name + " as TP provider"
            }
            tpProvider.save(failOnError: true)
        }

        redirect(action: 'list')
    }//end edit()

    @PreAuthorize('hasAuthority("tpat-admin")')
    def update(EditProviderCommand command) {
        log.info("Processing EditProviderCommand...")
        if( command.hasErrors() ){
            log.warn("Found errors in EditProviderCommand...")
            for( ObjectError oe : command.errors.allErrors ){
                log.warn("   FORM ERROR -> "+message(error:oe));
            }
            return render(view: '/provider/edit', model: [command: command]);
        }

        log.info("Valid EditProviderCommand[${command.name}, ${command.uri}] given, saving to database...");

        Provider.withTransaction {
            Provider provider = Provider.findById(command.id);
            if (provider == null)
                throw new ServletException("Cannot update unknown provider " + command.id);

            provider.setName(command.getName()?.trim() ?: "");
            provider.setUri(command.getUri()?.trim() ?: "");
            provider.setResponder(command.getResponder()?.trim() ?: "");
            provider.setEmail(command.getEmail()?.trim() ?: "");
            provider.setMailingAddress(command.getMailingAddress()?.trim() ?: "");
            provider.setTelephone(command.getTelephone()?.trim() ?: "");
            provider.setNotes(command.getNotes()?.trim() ?: "");
            provider.setTd(command.getTd());
            provider.setTp(command.getTp());

            provider.save(failOnError: true)
        }

        flash.message = "Successfully saved provider "+command.name
        redirect(action: 'list')

    }//end update()

    @PreAuthorize('hasAuthority("tpat-admin")')
    def delete() {
        log.info("Request to delete provider[${params.id}]...")
        if( StringUtils.isBlank(params.id) )
            throw new ServletException("Missing required param: id")

        Provider.withTransaction {
            Provider p = Provider.findByName(params.id);
            if (p == null)
                throw new ServletException("Could not find Provider: " + params.id);

            p.delete(flush: true)

            flash.message = "Successfully deleted provider '"+p.name+"'";
        }
        redirect(action: 'list')
    }//end delete()

    //==================================================================================================================
    //  Private Helper Methods
    //==================================================================================================================


}//end ProviderController

class CreateProviderCommand {
    String uri
    String name
    String responder
    String email
    String telephone
    String mailingAddress
    String notes
    Boolean td = false
    Boolean tp = false

    static constraints = {
        uri(nullable: false, blank: false, maxSize: 1024, url: true, validator: {val, obj, errors ->
            if( Provider.findByUri(val) )
                errors.rejectValue('uri', 'createProviderCommand.uri.exists', [val] as Object[], "URL '${val}' already exists in the database.");
        })
        name(nullable: false, blank: false, maxSize: 512, validator: {val, obj, errors ->
            if( Provider.findByName(val) )
                errors.rejectValue('name', 'createProviderCommand.name.exists', [val] as Object[], "Name '${val}' already exists in the database.");
        })

        responder     (nullable: true, blank: true, maxSize: 512)
        email         (nullable: true, blank: false, email: true, maxSize: 512)
        telephone     (nullable: true, blank: true, maxSize: 512, matches: "[0-9]{3}\\-[0-9]{3}\\-[0-9]{4}.*")
        mailingAddress(nullable: true, blank: true, maxSize: 512)
        notes         (nullable: true, blank: true, maxSize: 65532)
        td            (nullable: true)
        tp            (nullable: true)
    }
}

class EditProviderCommand {
    public static final Logger log = LoggerFactory.getLogger(EditProviderCommand.class);

    public void setData(Provider p){
        this.id = p.id;
        this.uri = p.uri;
        this.name = p.name;
        this.responder = p.responder;
        this.email = p.email;
        this.telephone = p.telephone;
        this.mailingAddress = p.mailingAddress;
        this.notes = p.notes;
        this.td = p.td;
        this.tp = p.tp;
    }

    Long id
    String uri
    String name
    String responder
    String email
    String telephone
    String mailingAddress
    String notes
    Boolean td
    Boolean tp

    static constraints = {
        id(nullable: false)

        uri(nullable: false, blank: false, maxSize: 1024, url: true, validator: {val, obj, errors ->
            log.debug("Validating the URI...")
            def p = Provider.findByUri(val)
            if( p && p.id != obj.id ) {
                errors.rejectValue('uri', 'editProviderCommand.uri.exists', [val] as Object[], "URL '${val}' already exists in the database.");
            }
        })

        name(nullable: false, blank: false, maxSize: 512, validator: {val, obj, errors ->
            log.debug("Validating the Name...")
            def p = Provider.findByName(val)
            if( p && p.id != obj.id ) {
                errors.rejectValue('name', 'editProviderCommand.name.exists', [val] as Object[], "Name '${val}' already exists in the database.");
            }
        })

        responder       (nullable: true, blank: true, maxSize: 512)
        email           (nullable: true, blank: false, email: true, maxSize: 512)
        telephone       (nullable: true, blank: true, maxSize: 512, matches: "[0-9]{3}\\-[0-9]{3}\\-[0-9]{4}.*")
        mailingAddress  (nullable: true, blank: true, maxSize: 512)
        notes           (nullable: true, blank: true, maxSize: 65532)
        td              (nullable: true, blank: true)
        tp              (nullable: true, blank: true)
    }
}
