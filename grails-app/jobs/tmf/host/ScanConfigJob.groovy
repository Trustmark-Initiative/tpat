package tmf.host

import tmf.host.util.QuartzConfig
import tmf.host.util.TFAMPropertiesHolder


/**
 * Created by brad on 1/22/16.
 */
class ScanConfigJob {
    //==================================================================================================================
    // Job Specifics
    //==================================================================================================================
    def sessionRequired = true
    def concurrent = false
    def description = "A job which periodically scans the system."
    def sessionFactory;
    //==================================================================================================================
    // Triggers
    //==================================================================================================================
    static triggers = {
        QuartzConfig config = TFAMPropertiesHolder.getQuartzConfig("scanjob");
        if( config.getType() == QuartzConfig.TriggerType.cron ){
            cron cronExpression: config.getCronExpression(), startDelay: config.getStartDelay()
        }
        if( config.getType() == QuartzConfig.TriggerType.interval ) {
            simple repeatInterval: config.getRepeatInterval(), startDelay: config.getStartDelay(), repeatCount: config.getRepeatCount()
        }
    }

    //==================================================================================================================
    // Execute entry point
    //==================================================================================================================
    void execute() {
        log.info("Starting ${this.getClass().getSimpleName()}...");
        long overallStartTime = System.currentTimeMillis();

        // TODO Anything you need done?

        long overallStopTime = System.currentTimeMillis();
        log.info("Successfully Executed ${this.getClass().getSimpleName()} in ${(overallStopTime - overallStartTime)}ms.")
    }//end execute()


    //==================================================================================================================
    // Instance Variables
    //==================================================================================================================

    //==================================================================================================================
    // General Helper Methods
    //==================================================================================================================

    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
//        propertyInstanceMap.get().clear()
    }


}/* End tmf.host.ScanConfigJob */