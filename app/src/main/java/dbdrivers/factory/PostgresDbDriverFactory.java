package dbdrivers.factory;

import dbdrivers.DbDriver;
import dbdrivers.postgres.PostgresDbDriver;
import static models.EnvVars.ALTER_SQL_SCRIPT_PATH;
import static models.EnvVars.DB_DATABASE;
import static models.EnvVars.DB_HOST;
import static models.EnvVars.DB_PASSWORD;
import static models.EnvVars.DB_PORT;
import static models.EnvVars.DB_SCHEMA;
import static models.EnvVars.DB_USERNAME;
import static models.EnvVars.INIT_SQL_SCRIPT_PATH;
import static models.EnvVars.IS_DISTRIBUTED_DATABASE;

import org.checkerframework.checker.nullness.qual.NonNull;

import models.db.drivers.postgres.PostgresDbDriverOptions;

public class PostgresDbDriverFactory implements DbDriverFactory {
    @Override
    @NonNull
    public DbDriver getDbDriver() {
        final PostgresDbDriverOptions postgresDbDriverOptions = new PostgresDbDriverOptions(
            System.getenv(DB_HOST),
            Integer.parseInt(System.getenv(DB_PORT)),
            System.getenv(DB_DATABASE),
            System.getenv(DB_SCHEMA),
            System.getenv(DB_USERNAME),
            System.getenv(DB_PASSWORD),
            System.getenv(INIT_SQL_SCRIPT_PATH),
            System.getenv(ALTER_SQL_SCRIPT_PATH),
            System.getenv(IS_DISTRIBUTED_DATABASE)
        );
        return new PostgresDbDriver(postgresDbDriverOptions);
    }
}