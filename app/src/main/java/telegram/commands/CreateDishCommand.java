package telegram.commands;

public class CreateDishCommand extends AbstractCommand {
    public CreateDishCommand(
        final String commandName,
        final String commandDescription
    ) {
        super(commandName, commandDescription);
    }

    @Override
    void execute() {}
}