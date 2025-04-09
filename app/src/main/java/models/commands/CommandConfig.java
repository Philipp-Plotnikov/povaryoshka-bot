package models.commands;

import language.ru.command.settings.*;
import org.checkerframework.checker.nullness.qual.NonNull;


final public class CommandConfig {
    @NonNull
    public final static CommandSettings CREATE_DISH_COMMAND_SETTINGS = new CommandSettings(
        CreateDish.COMMAND_NAME,
        CreateDish.COMMAND_DESCRIPTION
    );

    @NonNull
    public final static CommandSettings DELETE_DISH_COMMAND_SETTINGS = new CommandSettings(
        DeleteDish.COMMAND_NAME,
        DeleteDish.COMMAND_DESCRIPTION
    );

    @NonNull
    public final static CommandSettings UPDATE_DISH_COMMAND_SETTINGS = new CommandSettings(
        UpdateDish.COMMAND_NAME,
        UpdateDish.COMMAND_DESCRIPTION
    );

    @NonNull
    public final static CommandSettings GET_DISH_COMMAND_SETTINGS = new CommandSettings(
        GetDish.COMMAND_NAME,
        GetDish.COMMAND_DESCRIPTION
    );

    @NonNull
    public final static CommandSettings FEEDBACK_COMMAND_SETTINGS = new CommandSettings(
        Feedback.COMMAND_NAME,
        Feedback.COMMAND_DESCRIPTION
    );

    @NonNull
    public final static CommandSettings END_COMMAND_SETTINGS = new CommandSettings(
        End.COMMAND_NAME,
        End.COMMAND_DESCRIPTION
    );

    @NonNull
    public final static CommandSettings START_COMMAND_SETTINGS = new CommandSettings(
            Start.COMMAND_NAME,
            Start.COMMAND_DESCRIPTION
    );
}