package telegram.commands;

public class EndCommand extends AbstractCommand {
    public EndCommand(
        final String commandName,
        final String commandDescription
    ) {
        super(commandName, commandDescription);
    }

    @Override
    void execute() {}
}