package tmf.host.util;

import java.util.Properties;

/**
 * Created by brad on 5/7/17.
 */
public class QuartzConfig {

    public static enum TriggerType {
        cron,
        interval;

        public static TriggerType fromSting(String str){
            TriggerType type = null;
            if( str != null && str.length() > 0 ) {
                for (TriggerType t : TriggerType.values()) {
                    if (t.toString().equalsIgnoreCase(str.trim()) ){
                        type = t;
                        break;
                    }
                }
            }
            return type;
        }
    }

    public QuartzConfig(String name){
        this.name = name;
    }

    public QuartzConfig(Properties props, String name){
        this.name = name;
        this.setData(props);
    }

    private String name;

    private TriggerType type;
    private Long startDelay;

    private Long repeatInterval;
    private Long repeatCount;

    private String cronExpression;


    public void setData(Properties props){
        String triggerType = props.getProperty(this.name+".triggerType");
        this.type = TriggerType.fromSting(triggerType);

        String startDelayStr = props.getProperty(this.name+".startDelay");
        if( startDelayStr != null )
            this.startDelay = Long.parseLong(startDelayStr);
        else
            this.startDelay = 0l;

        if( this.type == TriggerType.cron ){
            this.cronExpression = props.getProperty(this.name+".cronExpression");
        }else if( this.type == TriggerType.interval ){
            String repeatCountStr = props.getProperty(this.name+".repeatCount");
            if( repeatCountStr != null )
                this.repeatCount = Long.parseLong(repeatCountStr);
            else
                this.repeatCount = -1l;

            this.repeatInterval = Long.parseLong(this.name+".repeatInterval");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TriggerType getType() {
        return type;
    }

    public void setType(TriggerType type) {
        this.type = type;
    }

    public Long getStartDelay() {
        return startDelay;
    }

    public void setStartDelay(Long startDelay) {
        this.startDelay = startDelay;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public Long getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Long repeatCount) {
        this.repeatCount = repeatCount;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
}
