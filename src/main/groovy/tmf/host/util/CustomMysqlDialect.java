package tmf.host.util;

import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.IntegerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Insert Comment Here
 * <br/><br/>
 *
 * @author brad
 * @date 6/26/17
 */
public class CustomMysqlDialect extends MySQL5Dialect {

    private static final Logger log = LoggerFactory.getLogger(CustomMysqlDialect.class);

    public CustomMysqlDialect(){
        super();

        log.debug("Adding custom Mysql regexp function...");
        this.registerFunction("regexp", new SQLFunctionTemplate(new IntegerType(), "?1 REGEXP ?2") );

    }


}/* end CustomMysqlDialect */