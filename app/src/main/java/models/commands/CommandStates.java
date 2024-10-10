package models.commands;

public enum CommandStates {
    DISH_NAME("dish_name"),
    INGREDIENTS("ingredients"),
    RECIPE("recipe");

    private final String value;

    private CommandStates(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}