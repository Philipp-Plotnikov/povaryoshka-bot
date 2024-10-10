package telegram.commands;

public class UpdateDishCommand extends AbstractCommand {
    public UpdateDishCommand(
        final String commandName,
        final String commandDescription
    ) {
        super(commandName, commandDescription);
    }

    @Override
    void execute() {}
}