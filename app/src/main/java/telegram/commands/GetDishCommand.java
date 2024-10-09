package telegram.commands;

public class GetDishCommand extends AbstractCommand {
    public GetDishCommand(
        final String commandName,
        final String commandDescription
    ) {
        super(commandName, commandDescription);
    }

    @Override
    void execute() {}
}