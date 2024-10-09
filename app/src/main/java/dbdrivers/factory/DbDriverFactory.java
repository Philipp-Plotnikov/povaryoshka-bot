package dbdrivers.factory;

import dbdrivers.DbDriver;

public interface DbDriverFactory {
    DbDriver getDbDriver();
}