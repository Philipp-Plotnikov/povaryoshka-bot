package models.commands;

public class CommandConfig {
    public final static CommandSettings CREATE_DISH_COMMAND_SETTINGS = new CommandSettings(
        "create-dish",
        "создает новое блюдо"
    );
    public final static CommandSettings DELETE_DISH_COMMAND_SETTINGS = new CommandSettings(
        "delete-dish",
        "удаляет существующее блюдо"
    );
    public final static CommandSettings UPDATE_DISH_COMMAND_SETTINGS = new CommandSettings(
        "update-dish",
        "позволяет отредактировать существующее блюдо (название, ингредиенты, описание)"
    );
    public final static CommandSettings GET_DISH_COMMAND_SETTINGS = new CommandSettings(
        "get-dish",
        "выдает ингредиенты и описание блюда по названию"
    );
    public final static CommandSettings FEEDBACK_COMMAND_SETTINGS = new CommandSettings(
        "feedback",
        "позволяет оставить обратную связь о работе с ботом: замечания, пожелания или просто свои впечатления"
    );
    public final static CommandSettings END_COMMAND_SETTINGS = new CommandSettings(
        "end",
        "позволяет прервать текущий процесс (например, создание), не закончив его полностью"
    );
}