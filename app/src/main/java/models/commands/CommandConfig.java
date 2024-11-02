package models.commands;

import org.checkerframework.checker.nullness.qual.NonNull;

public class CommandConfig {
    @NonNull
    public final static CommandSettings CREATE_DISH_COMMAND_SETTINGS = new CommandSettings(
        "create",
        "создает новое блюдо"
    );

    @NonNull
    public final static CommandSettings DELETE_DISH_COMMAND_SETTINGS = new CommandSettings(
        "delete",
        "удаляет существующее блюдо"
    );

    @NonNull
    public final static CommandSettings UPDATE_DISH_COMMAND_SETTINGS = new CommandSettings(
        "update",
        "позволяет отредактировать существующее блюдо (название, ингредиенты, описание)"
    );

    @NonNull
    public final static CommandSettings GET_DISH_COMMAND_SETTINGS = new CommandSettings(
        "get",
        "выдает ингредиенты и описание блюда по названию"
    );

    @NonNull
    public final static CommandSettings FEEDBACK_COMMAND_SETTINGS = new CommandSettings(
        "feedback",
        "позволяет оставить обратную связь о работе с ботом: замечания, пожелания или просто свои впечатления"
    );

    @NonNull
    public final static CommandSettings END_COMMAND_SETTINGS = new CommandSettings(
        "end",
        "позволяет прервать текущий процесс (например, создание), не закончив его полностью"
    );
}