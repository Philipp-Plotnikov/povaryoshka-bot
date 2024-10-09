package telegram.commands;

public class DeleteDishCommand extends AbstractCommand {
    public DeleteDishCommand(
        final String commandName,
        final String commandDescription
    ) {
        super(commandName, commandDescription);
    }

    @Override
    void execute() {}
}