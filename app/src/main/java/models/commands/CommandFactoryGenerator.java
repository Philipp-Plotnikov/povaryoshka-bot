package models.commands;

import org.checkerframework.checker.nullness.qual.NonNull;

import telegram.commands.factory.CommandFactory;


@FunctionalInterface
public interface CommandFactoryGenerator {
    @NonNull
    CommandFactory generate() throws Exception;
}