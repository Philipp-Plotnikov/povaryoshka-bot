package dbdrivers.factory;

import org.checkerframework.checker.nullness.qual.NonNull;

import dbdrivers.DbDriver;

public interface DbDriverFactory {
    @NonNull
    DbDriver getDbDriver();
}