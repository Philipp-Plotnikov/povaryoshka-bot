package models.db.drivers;

import dbdrivers.factory.DbDriverFactory;

@FunctionalInterface
public interface DbDriverFactoryGenerator {
    DbDriverFactory generate() throws Exception;
}