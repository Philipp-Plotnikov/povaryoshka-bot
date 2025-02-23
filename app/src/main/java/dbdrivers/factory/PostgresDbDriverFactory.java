package dbdrivers.factory;

import dbdrivers.IDbDriver;
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

public class PostgresDbDriverFactory implements IDbDriverFactory {
    @Override
    @NonNull
    public IDbDriver getDbDriver() throws SQLException
    {
        final PostgresDbDriverOptions postgresDbDriverOptions = new PostgresDbDriverOptions(
            System.getProperty(DB_HOST),
            Integer.parseInt(System.getProperty(DB_PORT)),
            System.getProperty(DB_DATABASE),
            System.getProperty(DB_SCHEMA),
            System.getProperty(DB_USERNAME),
            System.getProperty(DB_PASSWORD),
            System.getProperty(INIT_SQL_SCRIPT_PATH),
            System.getProperty(ALTER_SQL_SCRIPT_PATH),
            System.getProperty(IS_DISTRIBUTED_DATABASE)
        );
        return new PostgresDbDriver(postgresDbDriverOptions);
    }
}