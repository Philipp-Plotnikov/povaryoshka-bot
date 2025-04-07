package utilities;

import static models.system.EnvVars.DB_TYPE;

import org.checkerframework.checker.nullness.qual.NonNull;

import models.db.DbTypes;


final public class CommonsUtilities {
    @NonNull
    public static DbTypes getDbType() {
        return DbTypes.valueOf(System.getProperty(DB_TYPE).toUpperCase());
    }
}
