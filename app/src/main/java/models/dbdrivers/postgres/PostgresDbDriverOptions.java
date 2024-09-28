package models.dbdrivers.postgres;

import models.dbdrivers.AbstractDbDriverOptions;

public class PostgresDbDriverOptions extends AbstractDbDriverOptions {
    private final String DB_HOST;
    private final String DB_PORT;
    private final String DB_DATABASE;
    private final String DB_SCHEMA;
    private final String DB_USERNAME;
    private final String DB_PASSWORD;
    private final String DB_URL;

    public PostgresDbDriverOptions(
        final String dbHost,
        final String dbPort,
        final String dbDatabase,
        final String dbSchema,
        final String dbUsername,
        final String dbPassword
    ) {
        this.DB_HOST = dbHost;
        this.DB_PORT = dbPort;
        this.DB_DATABASE = dbDatabase;
        this.DB_SCHEMA = dbSchema;
        this.DB_USERNAME = dbUsername;
        this.DB_PASSWORD = dbPassword;
        this.DB_URL = this.constructAndGetDbUrl();
    }

    @Override
    public String getDbUrl() {
        return this.DB_URL;
    }

    @Override
    public String getDbSchema() {
        return this.DB_SCHEMA;
    }

    @Override
    public String getDbUsername() {
        return this.DB_USERNAME;
    }

    @Override
    public String getDbPassword() {
        return this.DB_PASSWORD;
    }

    private String constructAndGetDbUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s",
            this.DB_HOST,
            this.DB_PORT,
            this.DB_DATABASE
        );
    }
}