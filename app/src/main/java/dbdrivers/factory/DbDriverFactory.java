package dbdrivers.factory;

import java.sql.SQLException;

import org.checkerframework.checker.nullness.qual.NonNull;

import dbdrivers.DbDriver;

public interface DbDriverFactory {
    @NonNull
    DbDriver getDbDriver() throws SQLException;
}