package tmf.host

class UrlMappings {

    // tds and tips mappings must match TFAMPropertiesHolder.groovy
    // @see http://grails.github.io/grails-doc/3.0.x/guide/single.html#urlmappings (section 7.4.8)
    // static excludes = ["/static/**"]

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(controller: 'index', action: 'index')

        "/tds"(controller: 'trustmarkDefinition', action: 'list')
        "/tds/count"(controller: 'trustmarkDefinition', action: 'count')
        "/tds/list-by-name"(controller: 'trustmarkDefinition', action: 'listByName')
        "/tds/$id"(controller: 'trustmarkDefinition', action: 'view')
        "/tds/$tdName/$tdVersion"(controller: 'trustmarkDefinition', action: 'view')
//        "/tds/$tdMoniker/$tdVersion"(controller: 'trustmarkDefinition', action: 'resolve')

        "/tips"(controller: 'tip', action: 'list')
        "/tips/count"(controller: 'tip', action: 'count')
        "/tips/list-by-name"(controller: 'tip', action: 'listByName')
        "/tips/tip-tree.html"(controller: 'tip', action: 'tipTree')
        "/tips/top-level"(controller: 'tip', action: 'topLevelTipsListing')
        "/tips/$id"(controller: 'tip', action: 'view')
        "/tips/$id/tree"(controller: 'tip', action: 'viewTipTree')
        "/tips/$tipName/$tipVersion"(controller: 'tip', action: 'view')


        "/version-sets"(controller: 'versionSet', action: 'index')
        "/version-sets/create"(controller: 'versionSet', action: 'create')
        "/version-sets/save"(controller: 'versionSet', action: 'save')
        "/version-sets/$id"(controller: 'versionSet', action: 'show')
        "/version-sets/$id/creating"(controller: 'versionSet', action: 'showCreating')
        "/version-sets/$id/creating-status"(controller: 'versionSet', action: 'createVersionSetStatus')
        "/version-sets/$id/moveToProduction"(controller: 'versionSet', action: 'moveToProduction')
        "/version-sets/$id/resetDevelopment"(controller: 'versionSet', action: 'resetDevelopment')
        "/version-sets/$id/unlock"(controller: 'versionSet', action: 'unlock')
        "/version-sets/$id/tds"(controller: 'versionSet', action: 'trustmarkDefinitions')
        "/version-sets/$id/tds/$tdName/$tdVersion"(controller: 'versionSet', action: 'showTrustmarkDefinition')
        "/version-sets/$id/tips"(controller: 'versionSet', action: 'trustInteroperabilityProfiles')
        "/version-sets/$id/tips/$tipName/$tipVersion"(controller: 'versionSet', action: 'showTrustInteroperabilityProfile')


        "/version-sets/$id/edit"(controller: 'versionSetEdit', action: 'index')
        "/version-sets/$versionSetName/edit/process-upload/$id"(controller: 'versionSetEdit', action: 'processFileUpload')
        "/version-sets/$versionSetName/edit/process-upload/$id/choose-actions"(controller: 'versionSetEdit', action: 'chooseActionSummary')
        "/version-sets/$versionSetName/edit/process-upload/$id/apply-changes"(controller: 'versionSetEdit', action: 'applyChangesView')
        "/version-sets/$versionSetName/edit/process-upload/$id/apply-changes/start"(controller: 'versionSetEdit', action: 'applyChanges')
        "/version-sets/$versionSetName/edit/process-upload/$id/apply-changes/status"(controller: 'versionSetEdit', action: 'applyChangesStatus')
        "/version-sets/$id/edit/tds"(controller: 'versionSetEdit', action: 'trustmarkDefinitions')
        "/version-sets/$id/edit/tds/$linkId/delete"(controller: 'versionSetEdit', action: 'deleteTrustmarkDefinition')
        //@deprecated
        "/version-sets/$id/edit/tds/$linkId/edit"(controller: 'versionSetEdit', action: 'editTrustmarkDefinition')
        //@deprecated
        "/version-sets/$id/edit/tds/$linkId/save"(controller: 'versionSetEdit', action: 'saveTrustmarkDefinition')
        "/version-sets/$id/edit/tips"(controller: 'versionSetEdit', action: 'trustInteroperabilityProfiles')
        "/version-sets/$id/edit/tips/create"(controller: 'versionSetEdit', action: 'createTrustInteroperabilityProfile')
        "/version-sets/$id/edit/tips/$linkId/delete"(controller: 'versionSetEdit', action: 'deleteTIP')
        "/version-sets/$id/edit/tips/$linkId/edit"(controller: 'versionSetEdit', action: 'editTrustInteroperabilityProfile')
        "/version-sets/$id/edit/tips/$linkId/save"(controller: 'versionSetEdit', action: 'saveTrustInteroperabilityProfile')
        "/version-sets/$id/edit/tips/create-simple"(controller: 'versionSetEdit', action: 'simpleTipEditor')
        "/version-sets/$id/edit/tips/save-simple"(controller: 'versionSetEdit', action: 'saveSimpleTip')

        "/keywords"(controller: 'keyword', action: 'list')
        "/keywords/$id"(controller: 'keyword', action: 'view')

        "/providers"            (controller: 'provider', action:'list')
        "/providers/create"     (controller: 'provider', action:'create')
        "/providers/save"       (controller: 'provider', action:'save')
        "/providers/$id"        (controller: 'provider', action:'show')
        "/providers/$id/delete" (controller: 'provider', action:'delete')
        "/providers/$id/edit"   (controller: 'provider', action:'edit')
        "/providers/$id/update" (controller: 'provider', action:'update')
        "/providers/$id/settd"  (controller: 'provider', action:'setTd')
        "/providers/$id/settp"  (controller: 'provider', action:'setTp')

        "/taxonomy-terms"(controller:'taxonomyTerm', action: 'index')
        "/taxonomy-terms/synchronize"(controller:'taxonomyTerm', action: 'synchronize')

        "/appearance/banner"(controller:'appearance', action: 'banner')
        "/appearance/title"(controller:'appearance', action: 'title')

        "/chpasswd"(controller:'chpasswd', action: 'index')

        "500"(controller: 'errors', action: 'servletError')
        "404"(controller: 'errors', action: 'notFound')

    }
}
