package models.dbdrivers;

public interface DbDriverOptions {
    public String getDbSchema();

    public String getDbUsername();

    public String getDbPassword();

    public String getInitSQLScriptPath();

    public String getAlterSQLScriptPath();

    public boolean getIsDistributedDatabase();

    public String getDbUrl();
}