package models.commands;

import telegram.commands.factory.CommandFactory;

@FunctionalInterface
public interface CommandFactoryGenerator {
    CommandFactory generate() throws Exception;
}