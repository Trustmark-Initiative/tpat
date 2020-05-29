package nstic.util

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent

import static org.fusesource.jansi.Ansi.*

/**
 * Created by brad on 3/8/16.
 */
public class JansiPatternLayout extends PatternLayout {

    @Override
    public String doLayout(ILoggingEvent event) {
        String outputString = super.doLayout(event);
        return org.fusesource.jansi.Ansi.ansi().render(outputString).toString();
    }

}