package dbdrivers.factory;

import dbdrivers.IDbDriver;
import dbdrivers.postgres.PostgresDbDriver;

import java.sql.SQLException;

import org.checkerframework.checker.nullness.qual.NonNull;

import models.db.drivers.postgres.PostgresDbDriverOptions;
import utilities.PostgresDbDriverUtilities;


public class PostgresDbDriverFactory implements IDbDriverFactory {
    @Override
    @NonNull
    public IDbDriver getDbDriver() throws SQLException {
        final PostgresDbDriverOptions postgresDbDriverOptions = PostgresDbDriverUtilities.getPostgresDbDriverOptions();
        return new PostgresDbDriver(postgresDbDriverOptions);
    }
}