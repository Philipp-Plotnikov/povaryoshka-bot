package models.db.drivers.postgres;

import org.checkerframework.checker.nullness.qual.NonNull;

public class PostgresDbDriverOptions {
    @NonNull private final String DB_HOST;
    private final int DB_PORT;
    @NonNull private final String DB_DATABASE;
    @NonNull private final String DB_SCHEMA;
    @NonNull private final String DB_USERNAME;
    @NonNull private final String DB_PASSWORD;
    @NonNull private final String DB_URL;
    @NonNull private final String INIT_SQL_SCRIPT_PATH;
    @NonNull private final String ALTER_SQL_SCRIPT_PATH;
    private final boolean IS_DISTRIBUTED_DATABASE;

    public PostgresDbDriverOptions(
        @NonNull final String dbHost,
        final int dbPort,
        @NonNull final String dbDatabase,
        @NonNull final String dbSchema,
        @NonNull final String dbUsername,
        @NonNull final String dbPassword,
        @NonNull final String initSQLScriptPath,
        @NonNull final String alterSQLScriptPath,
        @NonNull final String isDistributedDatabase
    ) {
        DB_HOST = dbHost;
        DB_PORT = dbPort;
        DB_DATABASE = dbDatabase;
        DB_SCHEMA = dbSchema;
        DB_USERNAME = dbUsername;
        DB_PASSWORD = dbPassword;
        INIT_SQL_SCRIPT_PATH = initSQLScriptPath;
        ALTER_SQL_SCRIPT_PATH = alterSQLScriptPath;
        IS_DISTRIBUTED_DATABASE = Boolean.parseBoolean(isDistributedDatabase);
        DB_URL = constructAndGetDbUrl();
    }

    @NonNull
    public String getDbHost() {
        return DB_HOST;
    }

    public int getDbPort() {
        return DB_PORT;
    }

    @NonNull
    public String getDbDatabase() {
        return DB_DATABASE;
    }

    @NonNull
    public String getDbUrl() {
        return DB_URL;
    }

    @NonNull
    public String getDbSchema() {
        return DB_SCHEMA;
    }

    @NonNull
    public String getDbUsername() {
        return DB_USERNAME;
    }

    @NonNull
    public String getDbPassword() {
        return DB_PASSWORD;
    }

    @NonNull
    public String getInitSQLScriptPath() {
        return INIT_SQL_SCRIPT_PATH;
    }

    @NonNull
    public String getAlterSQLScriptPath() {
        return ALTER_SQL_SCRIPT_PATH;
    }

    public boolean getIsDistributedDatabase() {
        return IS_DISTRIBUTED_DATABASE;
    }

    @NonNull
    private String constructAndGetDbUrl() {
        final String dbUrlTemplate = "jdbc:postgresql://%s:%d/%s";
        return String.format(
            dbUrlTemplate,
            DB_HOST,
            DB_PORT,
            DB_DATABASE
        );
    }
}