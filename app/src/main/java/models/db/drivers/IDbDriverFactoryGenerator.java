package models.db.drivers;

import org.checkerframework.checker.nullness.qual.NonNull;

import dbdrivers.factory.IDbDriverFactory;

@FunctionalInterface
public interface IDbDriverFactoryGenerator {
    @NonNull
    IDbDriverFactory generate() throws Exception;
}