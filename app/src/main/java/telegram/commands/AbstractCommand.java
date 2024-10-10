package telegram.commands;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

public abstract class AbstractCommand extends BotCommand {
    protected AbstractCommand(
        final String commandName,
        final String commandDescription
    ) {
        super(commandName, commandDescription);
    }

    abstract void execute();
}