package dbdrivers.factory;

import dbdrivers.DbDriver;
import dbdrivers.postgres.PostgresDbDriver;
import static models.system.EnvVars.ALTER_SQL_SCRIPT_PATH;
import static models.system.EnvVars.DB_DATABASE;
import static models.system.EnvVars.DB_HOST;
import static models.system.EnvVars.DB_PASSWORD;
import static models.system.EnvVars.DB_PORT;
import static models.system.EnvVars.DB_SCHEMA;
import static models.system.EnvVars.DB_USERNAME;
import static models.system.EnvVars.INIT_SQL_SCRIPT_PATH;
import static models.system.EnvVars.IS_DISTRIBUTED_DATABASE;

import java.sql.SQLException;

import org.checkerframework.checker.nullness.qual.NonNull;

import models.db.drivers.postgres.PostgresDbDriverOptions;

public class PostgresDbDriverFactory implements DbDriverFactory {
    @Override
    @NonNull
    public DbDriver getDbDriver() throws SQLException
    {
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