package models.db.drivers.postgres;

public class PostgresDbDriverOptions {
    private final String DB_HOST;
    private final int DB_PORT;
    private final String DB_DATABASE;
    private final String DB_SCHEMA;
    private final String DB_USERNAME;
    private final String DB_PASSWORD;
    private final String DB_URL;
    private final String INIT_SQL_SCRIPT_PATH;
    private final String ALTER_SQL_SCRIPT_PATH;
    private final boolean IS_DISTRIBUTED_DATABASE;

    public PostgresDbDriverOptions(
        final String dbHost,
        final int dbPort,
        final String dbDatabase,
        final String dbSchema,
        final String dbUsername,
        final String dbPassword,
        final String initSQLScriptPath,
        final String alterSQLScriptPath,
        final String isDistributedDatabase
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

    public String getDbHost() {
        return DB_HOST;
    }

    public int getDbPort() {
        return DB_PORT;
    }

    public String getDbDatabase() {
        return DB_DATABASE;
    }

    public String getDbUrl() {
        return DB_URL;
    }

    public String getDbSchema() {
        return DB_SCHEMA;
    }

    public String getDbUsername() {
        return DB_USERNAME;
    }

    public String getDbPassword() {
        return DB_PASSWORD;
    }

    public String getInitSQLScriptPath() {
        return INIT_SQL_SCRIPT_PATH;
    }

    public String getAlterSQLScriptPath() {
        return ALTER_SQL_SCRIPT_PATH;
    }

    public boolean getIsDistributedDatabase() {
        return IS_DISTRIBUTED_DATABASE;
    }

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