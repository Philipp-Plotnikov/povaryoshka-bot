package telegram.commands.factory;

import java.util.Map;

import telegram.commands.AbstractCommand;

public interface CommandFactory {
    Map<String, AbstractCommand> getCommandMap();
}