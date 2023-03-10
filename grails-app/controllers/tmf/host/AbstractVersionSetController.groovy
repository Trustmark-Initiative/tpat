package tmf.host

import org.apache.commons.lang.StringUtils

import javax.servlet.ServletException

/**
 * TODO: Write a description here
 * @user brad
 * @date 11/22/16
 */
abstract class AbstractVersionSetController {

    /**
     * check if 2 dates come back with same date time, take the one lower on the list
     * @return
     */
    protected VersionSet resolveLatestVersionSet(){
        List<VersionSet> latestList = VersionSet.list([max: 2, sort: "dateCreated", order: "desc"])
        if( latestList.size() > 1 && latestList.get(0).equals(latestList.get(1)))
            return latestList.get(1)
        if( latestList.size() > 0 )
            return latestList.get(0)
        else
            return null
    }

    protected VersionSet getDevelopment()  {
        return VersionSet.findByDevelopment(true)
    }

    protected VersionSet getProduction()  {
        return VersionSet.findByProduction(true)
    }

    protected VersionSet resolveVersionSet(String value){
        log.debug "resolveVersionSet --> ${value}"

        if( StringUtils.isBlank(value) )
            throw new ServletException("The version set parameter is empty.")

        VersionSet vs = null

        try  {
            vs = VersionSet.get(value)
        } catch(Throwable T) { /* error is intentionally suppressed. */ }

        if( vs == null ){
            vs = VersionSet.findByName(value)
        }

        if(vs == null)  {
            log.warn("Could not find any version set: @|yellow "+value+"|@")
            throw new ServletException("Could not find any version set: " + value)
        }

        return vs;
    }
}
