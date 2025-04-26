package utilities;

import static models.system.EnvVars.DB_TYPE;

import org.checkerframework.checker.nullness.qual.NonNull;

import models.db.DbTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final public class CommonsUtilities {
    @NonNull
    private final static Logger logger = LoggerFactory.getLogger(CommonsUtilities.class);

    @NonNull
    public static DbTypes getDbType() {
        logger.debug("Got DbType");
        return DbTypes.valueOf(System.getProperty(DB_TYPE).toUpperCase());
    }
}
