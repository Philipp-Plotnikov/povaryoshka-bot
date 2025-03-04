package models.commands;

import org.checkerframework.checker.nullness.qual.NonNull;

import telegram.commands.factory.ICommandFactory;


@FunctionalInterface
public interface ICommandFactoryGenerator {
    @NonNull
    ICommandFactory generate() throws Exception;
}