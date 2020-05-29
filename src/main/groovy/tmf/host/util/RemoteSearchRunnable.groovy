package tmf.host.util

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.service.RemoteSearchResult
import edu.gatech.gtri.trustmark.v1_0.service.RemoteStatus
import edu.gatech.gtri.trustmark.v1_0.service.TrustmarkFrameworkService
import edu.gatech.gtri.trustmark.v1_0.service.TrustmarkFrameworkServiceFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by brad on 5/14/17.
 */
class RemoteSearchRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RemoteSearchRunnable.class);

    public RemoteSearchRunnable(String queryString){
        this.queryString = queryString;
    }

    private String queryString = null;
    private Map<RemoteStatus, RemoteSearchResult> searchResultMap;
    private Map<RemoteStatus, Throwable> searchResultErrorsMap;

    public String getQueryString() {
        return queryString
    }

    Map<RemoteStatus, RemoteSearchResult> getSearchResultMap() {
        if( searchResultMap == null )
            searchResultMap = new HashMap<>();
        return searchResultMap
    }

    Map<RemoteStatus, Throwable> getSearchResultErrorsMap() {
        if( searchResultErrorsMap == null )
            searchResultErrorsMap = new HashMap<>();
        return searchResultErrorsMap
    }


    @Override
    void run() {
        for (String registryUrl : TFAMPropertiesHolder.getRegistryUrlsAsStrings() ){
            TrustmarkFrameworkService service = FactoryLoader.getInstance(TrustmarkFrameworkServiceFactory.class).createService(registryUrl);
            try {
                RemoteStatus remoteStatus = service.getStatus();
                if (remoteStatus.supportsTrustInteroperabilityProfiles || remoteStatus.supportsTrustmarkDefinitions) {
                    try {
                        logger.info("Executing search[${this.queryString}] on Registry[$registryUrl]...")
                        RemoteSearchResult result = service.search(this.queryString);
                        this.getSearchResultMap().put(remoteStatus, result);
                    } catch (Throwable t) {
                        logger.error("Error executing search to [$registryUrl]: " + t);
                        this.getSearchResultErrorsMap().put(remoteStatus, t);
                    }
                }
            }catch(Throwable t2){
                logger.error("Error getting remote status of [${registryUrl}]: "+t2);
            }
        }
        logger.info("Successfully finished executing RemoteSearch thread!");
    }//end run()


}/* end RemoteSearchRunnable */