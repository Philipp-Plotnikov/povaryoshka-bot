package dbdrivers.factory;

import dbdrivers.IDbDriver;
import dbdrivers.postgres.PostgresDbDriver;

import java.sql.SQLException;

import org.checkerframework.checker.nullness.qual.NonNull;

import models.db.drivers.postgres.PostgresDbDriverOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.PostgresDbDriverUtilities;


final public class PostgresDbDriverFactory implements IDbDriverFactory {
    @NonNull
    private final static Logger logger = LoggerFactory.getLogger(PostgresDbDriver.class);

    @Override
    @NonNull
    public IDbDriver createDbDriver() throws SQLException {
        final PostgresDbDriverOptions postgresDbDriverOptions = PostgresDbDriverUtilities.getPostgresDbDriverOptions();
        logger.debug("Created DbDriver");
        return new PostgresDbDriver(postgresDbDriverOptions);
    }
}