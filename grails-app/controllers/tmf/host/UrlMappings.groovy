package tmf.host

class UrlMappings {

    // @see http://grails.github.io/grails-doc/3.0.x/guide/single.html#urlmappings (section 7.4.8)
    // static excludes = ["/static/**"]

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(controller: 'index', action: 'index')

        "/trustmark-definitions"(controller: 'trustmarkDefinition', action: 'list')
        "/trustmark-definitions/count"(controller: 'trustmarkDefinition', action: 'count')
        "/trustmark-definitions/list-by-name"(controller: 'trustmarkDefinition', action: 'listByName')
        "/trustmark-definitions/$id"(controller: 'trustmarkDefinition', action: 'view')
        "/trustmark-definitions/$tdName/$tdVersion"(controller: 'trustmarkDefinition', action: 'view')
//        "/trustmark-definitions/$tdMoniker/$tdVersion"(controller: 'trustmarkDefinition', action: 'resolve')

        "/trust-interoperability-profiles"(controller: 'tip', action: 'list')
        "/trust-interoperability-profiles/count"(controller: 'tip', action: 'count')
        "/trust-interoperability-profiles/list-by-name"(controller: 'tip', action: 'listByName')
        "/trust-interoperability-profiles/tip-tree.html"(controller: 'tip', action: 'tipTree')
        "/trust-interoperability-profiles/top-level"(controller: 'tip', action: 'topLevelTipsListing')
        "/trust-interoperability-profiles/$id"(controller: 'tip', action: 'view')
        "/trust-interoperability-profiles/$id/tree"(controller: 'tip', action: 'viewTipTree')
        "/trust-interoperability-profiles/$tipName/$tipVersion"(controller: 'tip', action: 'view')


        "/version-sets"(controller: 'versionSet', action: 'index')
        "/version-sets/create"(controller: 'versionSet', action: 'create')
        "/version-sets/save"(controller: 'versionSet', action: 'save')
        "/version-sets/$id"(controller: 'versionSet', action: 'show')
        "/version-sets/$id/creating"(controller: 'versionSet', action: 'showCreating')
        "/version-sets/$id/creating-status"(controller: 'versionSet', action: 'createVersionSetStatus')
        "/version-sets/$id/moveToProduction"(controller: 'versionSet', action: 'moveToProduction')
        "/version-sets/$id/resetDevelopment"(controller: 'versionSet', action: 'resetDevelopment')
        "/version-sets/$id/unlock"(controller: 'versionSet', action: 'unlock')
        "/version-sets/$id/trustmark-definitions"(controller: 'versionSet', action: 'trustmarkDefinitions')
        "/version-sets/$id/trustmark-definitions/$tdName/$tdVersion"(controller: 'versionSet', action: 'showTrustmarkDefinition')
        "/version-sets/$id/trust-interoperability-profiles"(controller: 'versionSet', action: 'trustInteroperabilityProfiles')
        "/version-sets/$id/trust-interoperability-profiles/$tipName/$tipVersion"(controller: 'versionSet', action: 'showTrustInteroperabilityProfile')


        "/version-sets/$id/edit"(controller: 'versionSetEdit', action: 'index')
        "/version-sets/$versionSetName/edit/process-upload/$id"(controller: 'versionSetEdit', action: 'processFileUpload')
        "/version-sets/$versionSetName/edit/process-upload/$id/choose-actions"(controller: 'versionSetEdit', action: 'chooseActionSummary')
        "/version-sets/$versionSetName/edit/process-upload/$id/apply-changes"(controller: 'versionSetEdit', action: 'applyChangesView')
        "/version-sets/$versionSetName/edit/process-upload/$id/apply-changes/start"(controller: 'versionSetEdit', action: 'applyChanges')
        "/version-sets/$versionSetName/edit/process-upload/$id/apply-changes/status"(controller: 'versionSetEdit', action: 'applyChangesStatus')
        "/version-sets/$id/edit/trustmark-definitions"(controller: 'versionSetEdit', action: 'trustmarkDefinitions')
        "/version-sets/$id/edit/trustmark-definitions/$linkId/delete"(controller: 'versionSetEdit', action: 'deleteTrustmarkDefinition')
        "/version-sets/$id/edit/trustmark-definitions/$linkId/edit"(controller: 'versionSetEdit', action: 'editTrustmarkDefinition')
        "/version-sets/$id/edit/trustmark-definitions/$linkId/save"(controller: 'versionSetEdit', action: 'saveTrustmarkDefinition')
        "/version-sets/$id/edit/trust-interoperability-profiles"(controller: 'versionSetEdit', action: 'trustInteroperabilityProfiles')
        "/version-sets/$id/edit/trust-interoperability-profiles/create"(controller: 'versionSetEdit', action: 'createTrustInteroperabilityProfile')
        "/version-sets/$id/edit/trust-interoperability-profiles/$linkId/delete"(controller: 'versionSetEdit', action: 'deleteTIP')
        "/version-sets/$id/edit/trust-interoperability-profiles/$linkId/edit"(controller: 'versionSetEdit', action: 'editTrustInteroperabilityProfile')
        "/version-sets/$id/edit/trust-interoperability-profiles/$linkId/save"(controller: 'versionSetEdit', action: 'saveTrustInteroperabilityProfile')
        "/version-sets/$id/edit/trust-interoperability-profiles/create-simple"(controller: 'versionSetEdit', action: 'simpleTipEditor')
        "/version-sets/$id/edit/trust-interoperability-profiles/save-simple"(controller: 'versionSetEdit', action: 'saveSimpleTip')

        "/keywords"(controller: 'keyword', action: 'list')
        "/keywords/$id"(controller: 'keyword', action: 'view')

        "/providers"(controller:'provider', action:'list')
        "/providers/create"(controller: 'provider', action:'create')
        "/providers/save"(controller: 'provider', action:'save')
        "/providers/$id"(controller: 'provider', action:'show')
        "/providers/$id/delete"(controller: 'provider', action:'delete')
        "/providers/$id/edit"(controller: 'provider', action:'edit')
        "/providers/$id/update"(controller: 'provider', action:'update')

        "/taxonomy-terms"(controller:'taxonomyTerm', action: 'index')
        "/taxonomy-terms/synchronize"(controller:'taxonomyTerm', action: 'synchronize')

        "/appearance"(controller:'appearance', action: 'index')
        "/appearance/banner"(controller:'appearance', action: 'banner')
        "/appearance/title"(controller:'appearance', action: 'title')

        "500"(controller: 'errors', action: 'servletError')
        "404"(controller: 'errors', action: 'notFound')

    }
}
