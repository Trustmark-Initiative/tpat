package tmf.host

import org.apache.commons.lang.StringUtils
import tmf.host.util.TFAMPropertiesHolder;

import edu.gatech.gtri.trustmark.v1_0.model.Entity
import tmf.host.util.TfamOwnerOrganization
import edu.gatech.gtri.trustmark.v1_0.model.Contact
import edu.gatech.gtri.trustmark.v1_0.model.ContactKindCode

/**
 * Presents an "Entity" in the Trustmark Framework that is capable of granting Trustmarks based on Trustmark Definitions.
 */
class Provider implements Comparable<Provider>{

    String uri
    String name

    // Simple contact information, does not represent EVERYTHING that can happen.
    String responder
    String email
    String telephone
    String mailingAddress
    String notes
    Boolean td = false
    Boolean tp = false


    static constraints = {
        uri             (nullable: false, blank: false, maxSize: 1024, unique: true, url: true)
        name            (nullable: false, blank: false, maxSize: 512,  unique: true)
        td              (nullable: false)
        tp              (nullable: false)

        responder       (nullable: true, blank: true, maxSize: 512)
        email           (nullable: true, blank: true, maxSize: 1024, email: true)
        telephone       (nullable: true, blank: true, maxSize: 512)
        mailingAddress  (nullable: true, blank: true, maxSize: 512)
        notes           (nullable: true, blank: true, maxSize: 65535)

    }

    static mapping = {
        notes type: 'text'
    }


    public Map toJson() {
        Map json = [
                Name: this.name,
                Identifier: this.uri,
                Uri: this.uri,
                Email: this.email
        ]

        if( StringUtils.isNotBlank(this.responder) )
            json.put("Responder", this.responder);

        if( StringUtils.isNotBlank(this.telephone) )
            json.put("Telephone", this.telephone);

        if( StringUtils.isNotBlank(this.mailingAddress) )
            json.put("MailingAddress", this.mailingAddress);

        if( StringUtils.isNotBlank(this.notes) )
            json.put("Notes", this.notes);

        return json;
    }


    public String toString() {
        return "Provider["+this.name+"]"
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public boolean equals(Object other){
        if( other != null && other instanceof Provider){
            return this.getName().equalsIgnoreCase(((Provider) other).getName());
        }
    }

    @Override
    int compareTo(Provider o) {
        if( o == null ) return -1;
        return this.getName().compareToIgnoreCase(o.getName());
    }

    static void populateProviders() {
        Provider.withTransaction {
            List<Provider> providers = Provider.findAll()
            if (providers.isEmpty()) {
                Provider provider
                TfamOwnerOrganization ownerOrganization = TFAMPropertiesHolder.getDefaultEntity()
                Contact contact
                contact = ownerOrganization.getDefaultContact()
                if(contact) {
                    //TD
                    if(!contact.websiteURLs.isEmpty()) {
                        provider = new Provider()
                        provider.setName("TDO")
                        provider.setUri(contact.websiteURLs.get(0)?.toString().trim() ?: "")
                        provider.setEmail(contact.getDefaultEmail().toString().trim() ?: "")
                        if (!contact.getTelephones().isEmpty()) {
                            provider.setTelephone(contact.getTelephones().get(0).toString().trim() ?: "")
                        }
                        provider.setTd(true)
                        provider.setTp(true)
                        provider.save(failOnError: true)
                        log.debug("Provider is added to DB: ${provider.name} ")
                    } else {
                        log.error("Unable to add Provider to DB since URL is not specified.")
                    }

                }
                List<Contact> contacts = ownerOrganization.getOtherContacts()
                if(!contacts.isEmpty()) {
                    //TP
                    for(Contact cur: contacts){
                        if(!contact.websiteURLs.isEmpty()) {
                            provider = new Provider()
                            provider.setName("Trustmark Provider")
                            provider.setUri(cur.websiteURLs.get(0)?.toString().trim() ?: "")
                            provider.setEmail(cur.getDefaultEmail().toString().trim() ?: "")
                            if (!contact.getTelephones().isEmpty()) {
                                provider.setTelephone(contact.getTelephones().get(0).toString().trim() ?: "")
                            }
                            provider.setTp(true)
                            provider.save(failOnError: true)
                            log.debug("Provider is added to DB: ${provider.name} ")
                        } else {
                            log.error("Unable to add Provider to DB since URL is not specified.")
                        }
                    }
                }
            } else {
                log.debug("DB already contains providers. Skipping...")
            }
        }
    }
}