package models.commands;

import org.checkerframework.checker.nullness.qual.NonNull;

public enum CommandStates {
    DISH_NAME("dish_name"),
    INGREDIENTS("ingredients"),
    RECIPE("recipe"),
    FEEDBACK("feedback");

    @NonNull
    private final String value;

    private CommandStates(@NonNull final String value) {
        this.value = value;
    }

    @NonNull
    public String getValue() {
        return value;
    }
}