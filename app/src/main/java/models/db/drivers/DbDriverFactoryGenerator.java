package models.db.drivers;

import org.checkerframework.checker.nullness.qual.NonNull;

import dbdrivers.factory.DbDriverFactory;

@FunctionalInterface
public interface DbDriverFactoryGenerator {
    @NonNull
    DbDriverFactory generate() throws Exception;
}