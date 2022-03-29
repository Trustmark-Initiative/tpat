package tmf.host.util

import edu.gatech.gtri.trustmark.v1_0.model.Contact
import edu.gatech.gtri.trustmark.v1_0.model.ContactKindCode
import edu.gatech.gtri.trustmark.v1_0.model.Entity

/**
 * Created by brad on 4/17/17.
 */
class TfamOwnerOrganization implements Entity {

    //==================================================================================================================
    //  Instance Variables
    //==================================================================================================================
    private String name;
    private URI identifier;
//    private String abbreviation;
//    private String logoImagePath;

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
//    public String getAbbreviation(){
//        return abbreviation;
//    }
//    public String getLogoImagePath() {
//        if( !logoImagePath.startsWith("/") )
//            return "/" + logoImagePath;
//        else
//            return logoImagePath;
//    }
    @Override
    public List<Contact> getContacts() {
        if( contacts == null )
            contacts = new ArrayList<>();
        return contacts
    }
    //==================================================================================================================
    //  Setters
    //==================================================================================================================
//    public void setAbbreviation(String abbreviation) {
//        this.abbreviation = abbreviation
//    }

//    public void setLogoImagePath(String logoImagePath) {
//        this.logoImagePath = logoImagePath
//    }

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

    public List <Contact> getOtherContacts(){
        List <Contact> otherContacts = new ArrayList<>();
        for( Contact cur : getContacts() ){
            if( cur.getKind() == ContactKindCode.OTHER ){
                otherContacts.add(cur)
            }
        }
        return otherContacts;
    }

    @Override
    public String toString(){
        return new StringBuilder("TfamOwnerOrganization [")
                .append(" name = ").append(name).append(";")
                .append(" identifier = ").append(identifier).append(";")
                .append(" contacts = (").append(contacts).append(")]")
                .toString()
    }
}
