package tmf.host.util

import edu.gatech.gtri.trustmark.v1_0.model.Contact
import edu.gatech.gtri.trustmark.v1_0.model.ContactKindCode
import tmf.host.Provider

/**
 * A simple, default implementation of a ContactImpl for use in the TFAM.
 * <br/><br/>
 * @user brad
 * @date 12/19/16
 */
class ContactImpl implements Contact {

    public ContactImpl(Provider provider){
        this.kind = ContactKindCode.PRIMARY;
        this.responder = provider.responder;
        if( provider.email?.length() > 0 ){
            this.getEmails().add(provider.email);
        }
        if( provider.mailingAddress?.length() > 0 ){
            this.getMailingAddresses().add(provider.mailingAddress);
        }
        if( provider.telephone?.length() > 0 ){
            this.getTelephones().add(provider.telephone);
        }
        if( provider.notes?.length() > 0 ){
            this.notes = provider.notes;
        }
        if( provider.uri?.length() > 0 ){
            this.websiteURLs.add(new URL(provider.uri))
        }
        if( provider.getTd()){
            this.kind = ContactKindCode.PRIMARY
        } else {
            this.kind = ContactKindCode.OTHER
        }
    }
    public ContactImpl(){
        this(ContactKindCode.PRIMARY);
    }
    public ContactImpl(ContactKindCode kind){
        this(kind, "");
    }
    public ContactImpl(ContactKindCode kind, String responder){
        this.kind = kind;
        this.responder = responder;
    }

    private ContactKindCode kind;
    private String responder;
    private List<String> emails = [];
    private List<String> telephones = [];
    private List<String> physicalAddresses = [];
    private List<String> mailingAddresses = [];
    private List<URL> websiteURLs = [];
    private String notes;

    def first(List things){
        if( things && things.size() > 0 )
            return things.get(0);
        else
            return null
    }

    @Override
    public String toString(){
        return new StringBuilder("Contact [")
                .append(" responder = ").append(responder).append(";")
                .append(" emails = ").append(emails).append(";")
                .append(" telephones = ").append(telephones).append(";")
                .append(" physicalAddresses = ").append(physicalAddresses).append(";")
                .append(" mailingAddresses = ").append(mailingAddresses).append(";")
                .append(" websiteURLs = ").append(websiteURLs).append(";")
                .append(" notes = ").append(notes).append("]")
                .toString()
    }

    @Override
    ContactKindCode getKind() {
        return kind;
    }
    @Override
    String getResponder() {
        return responder
    }
    @Override
    List<String> getEmails() {
        return emails
    }
    @Override
    String getDefaultEmail() {
        return first(emails)
    }
    @Override
    List<String> getTelephones() {
        return telephones
    }
    @Override
    String getDefaultTelephone() {
        return first(telephones)
    }
    @Override
    List<String> getPhysicalAddresses() {
        return physicalAddresses
    }
    @Override
    String getDefaultPhysicalAddress() {
        return first(physicalAddresses)
    }
    @Override
    List<String> getMailingAddresses() {
        return mailingAddresses
    }
    @Override
    String getDefaultMailingAddress() {
        return first(mailingAddresses)
    }
    @Override
    List<URL> getWebsiteURLs() {
        return websiteURLs
    }
    @Override
    URL getDefaultWebsiteURL() {
        return first(websiteURLs)
    }
    @Override
    String getNotes() {
        return notes;
    }
}
