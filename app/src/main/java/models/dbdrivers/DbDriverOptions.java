package models.dbdrivers;

public interface DbDriverOptions {
    abstract public String getDbUrl();

    abstract public String getDbSchema();

    abstract public String getDbUsername();

    abstract public String getDbPassword();
}