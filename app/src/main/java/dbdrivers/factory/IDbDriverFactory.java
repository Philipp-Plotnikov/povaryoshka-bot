package dbdrivers.factory;

import java.sql.SQLException;

import org.checkerframework.checker.nullness.qual.NonNull;

import dbdrivers.IDbDriver;


public interface IDbDriverFactory {
    @NonNull
    IDbDriver createDbDriver() throws SQLException;
}