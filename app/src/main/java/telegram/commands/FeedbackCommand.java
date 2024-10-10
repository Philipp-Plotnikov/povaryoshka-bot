package telegram.commands;

public class FeedbackCommand extends AbstractCommand {
    public FeedbackCommand(
        final String commandName,
        final String commandDescription
    ) {
        super(commandName, commandDescription);
    }

    @Override
    void execute() {}
}