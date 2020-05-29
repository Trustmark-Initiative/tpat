package tmf.host

import grails.plugin.springsecurity.annotation.Secured

/**
 * A controller for handling system variables directly.
 * <br/><br/>
 * @author brad
 * @date 3/3/17
 */
@Secured("ROLE_ADMIN")
class SystemVariableController {

    def index(){
        redirect(action: 'list')
    }

    def list() {
        log.info("Displaying system variables list...");
        [sysVars: SystemVariable.findAll([max: 6, offset: 0, sort: 'id', order: 'asc'])]
    }

    def delete() {
        log.info("Deleting SystemVariable[@|cyan ${params.varName}|@]...")
        SystemVariable.withTransaction {
            SystemVariable systemVariable = SystemVariable.findByName(params.varName);
            if (systemVariable) {
                systemVariable.delete();
            }
            flash.message = "Successfully deleted System Variable '${params.varName}'."
        }
        redirect(action:'list')
    }

    def edit() {
        log.info("Setting value for SystemVariable[${params.varName}] to [${params.value}]...")
        SystemVariable systemVariable = SystemVariable.findByName(params.varName);
        if( !systemVariable ){
            systemVariable = new SystemVariable(name: params.varName);
        }
        systemVariable.fieldValue = params.value
        systemVariable.save(failOnError: true);

        flash.message = "Successfully set value for System Variable '${params.varName}'."
        redirect(action:'list')
    }

}
