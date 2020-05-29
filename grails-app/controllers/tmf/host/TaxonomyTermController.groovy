package tmf.host

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.service.RemoteTaxonomy
import edu.gatech.gtri.trustmark.v1_0.service.RemoteTaxonomyTerm
import edu.gatech.gtri.trustmark.v1_0.service.TrustmarkFrameworkService
import edu.gatech.gtri.trustmark.v1_0.service.TrustmarkFrameworkServiceFactory
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import tmf.host.util.TFAMPropertiesHolder

/**
 * Created by brad on 4/27/17.
 */
@Secured("ROLE_ADMIN")
class TaxonomyTermController {

    static def scaffold = TaxonomyTerm.class

    def sessionFactory

    def index() {
        log.debug("Showing top level terms...");
        [terms: TaxonomyTerm.getTopLevelTerms()]
    }

    def list(){
        forward(action: 'index', params: params)
    }

    /**
     * This method will contact the registry and download all the Taxonomy Terms from it.  Any localized terms will be
     * overwritten.
     */
    def synchronize() {
        log.info("Synchronizing terms with remote servers...");
        log.info("Removing all pre-existing taxonomy terms...");
        TaxonomyTerm.executeUpdate("update TaxonomyTerm t set t.parent = null");
        TaxonomyTerm.executeUpdate("delete TaxonomyTerm");

        def response = [:]
        try{
            List<String> registryUrls = TFAMPropertiesHolder.getRegistryUrlsAsStrings();
            for( String registryUrl : registryUrls ){
                TrustmarkFrameworkService service = FactoryLoader.getInstance(TrustmarkFrameworkServiceFactory.class).createService(registryUrl);
                RemoteTaxonomy taxonomy = service.getTaxonomy();
                log.debug("Received @|cyan ${taxonomy.getTotalTermCount()}|@ remote terms from [@|green ${registryUrl}|@]")
                List<RemoteTaxonomyTerm> terms = taxonomy.getTerms();
                for( RemoteTaxonomyTerm term : terms ){
                    _recursivelyBuildTerm(term, null);
                }
            }
            response = [status: 'SUCCESS', message: "Successfully downloaded and cached remote taxonomy!"]
        }catch(Throwable t){
            log.error("Error working with remote taxonomy!", t);
            response = [status: 'ERROR', message: "Error while getting remote taxonomy: "+t]
        }

        render response as JSON
    }




    private void _recursivelyBuildTerm(RemoteTaxonomyTerm remoteTerm, TaxonomyTerm parent){
        TaxonomyTerm t = new TaxonomyTerm(name: remoteTerm.getTerm(), parent: parent);
        log.debug("Storing term: @|cyan ${t.name}|@")
        t.save(failOnError: true);
        for( RemoteTaxonomyTerm childTerm : remoteTerm.getChildren() ){
            _recursivelyBuildTerm(childTerm, t);
        }
    }

}
