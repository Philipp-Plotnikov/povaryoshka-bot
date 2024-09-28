package models.dbdrivers;

public abstract class AbstractDbDriverOptions {
    abstract public String getDbUrl();

    abstract public String getDbSchema();

    abstract public String getDbUsername();

    abstract public String getDbPassword();
}