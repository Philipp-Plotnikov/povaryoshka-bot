package models.dbdrivers.postgres;

import models.dbdrivers.DbDriverOptions;

public class PostgresDbDriverOptions implements DbDriverOptions {
    private final String DB_HOST;
    private final String DB_PORT;
    private final String DB_DATABASE;
    private final String DB_SCHEMA;
    private final String DB_USERNAME;
    private final String DB_PASSWORD;
    private final String DB_URL;
    private final String INIT_SQL_SCRIPT_PATH;
    private final String ALTER_SQL_SCRIPT_PATH;

    public PostgresDbDriverOptions(
        final String dbHost,
        final String dbPort,
        final String dbDatabase,
        final String dbSchema,
        final String dbUsername,
        final String dbPassword,
        final String initSQLScriptPath,
        final String alterSQLScriptPath
    ) {
        DB_HOST = dbHost;
        DB_PORT = dbPort;
        DB_DATABASE = dbDatabase;
        DB_SCHEMA = dbSchema;
        DB_USERNAME = dbUsername;
        DB_PASSWORD = dbPassword;
        INIT_SQL_SCRIPT_PATH = initSQLScriptPath;
        ALTER_SQL_SCRIPT_PATH = alterSQLScriptPath;
        DB_URL = constructAndGetDbUrl();
    }

    @Override
    public String getDbUrl() {
        return DB_URL;
    }

    @Override
    public String getDbSchema() {
        return DB_SCHEMA;
    }

    @Override
    public String getDbUsername() {
        return DB_USERNAME;
    }

    @Override
    public String getDbPassword() {
        return DB_PASSWORD;
    }

    @Override
    public String getInitSQLScriptPath() {
        return INIT_SQL_SCRIPT_PATH;
    }

    @Override
    public String getAlterSQLScriptPath() {
        return ALTER_SQL_SCRIPT_PATH;
    }

    private String constructAndGetDbUrl() {
        final String dbUrlTemplate = "jdbc:postgresql://%s:%s/%s";
        return String.format(
            dbUrlTemplate,
            DB_HOST,
            DB_PORT,
            DB_DATABASE
        );
    }
}