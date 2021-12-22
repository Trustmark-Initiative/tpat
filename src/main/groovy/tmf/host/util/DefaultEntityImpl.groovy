package tmf.host.util

import edu.gatech.gtri.trustmark.v1_0.model.Contact
import edu.gatech.gtri.trustmark.v1_0.model.ContactKindCode
import edu.gatech.gtri.trustmark.v1_0.model.Entity
import tmf.host.Provider

/**
 * Created by brad on 4/17/17.
 */
class DefaultEntityImpl implements Entity {

    public DefaultEntityImpl(){}
    public DefaultEntityImpl(Provider provider){
        this.name = provider.name;
        this.identifier = new URI(provider.uri);
        Contact c = new ContactImpl(provider);
        this.getContacts().add(c);
    }

    //==================================================================================================================
    //  Instance Variables
    //==================================================================================================================
    private String name;
    private URI identifier;
    private List<Contact> contacts = new ArrayList<>();
    //==================================================================================================================
    //  Getters
    //==================================================================================================================
    @Override
    public URI getIdentifier() {
        return identifier;
    }
    @Override
    public String getName() {
        return name
    }
    @Override
    public List<Contact> getContacts() {
        if( contacts == null )
            contacts = new ArrayList<>();
        return contacts
    }
    //==================================================================================================================
    //  Setters
    //==================================================================================================================
    public void setName(String name) {
        this.name = name
    }

    public void setIdentifier(URI identifier) {
        this.identifier = identifier
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts
    }

    //==================================================================================================================
    //  Public Methods
    //==================================================================================================================
    @Override
    public Contact getDefaultContact() {
        Contact c = null;
        for( Contact cur : getContacts() ){
            if( cur.getKind() == ContactKindCode.PRIMARY ){
                c = cur;
            }
        }
        if( c == null && getContacts().size() > 0 )
            c = getContacts().get(0);
        return c;
    }

    public String toString() {
        return String.format("Entity[ identifier = %s, name = %s , contacts = %s ]",
                this.getIdentifier() == null ? "" : this.getIdentifier().toString(),
                this.getName() == null ? "" : this.getName(),
                this.getContacts() == null ? "" : Arrays.toString(this.getContacts().toArray()));
    }
}
