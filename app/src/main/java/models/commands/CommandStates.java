package models.commands;

import org.checkerframework.checker.nullness.qual.NonNull;


public enum CommandStates {
    DISH_NAME("dish_name"),
    DISH_NAME_UPDATE_CONFIRM("dish_name_update_confirm"),
    DISH_NAME_UPDATE("dish_name_update"),
    INGREDIENTS_UPDATE_CONFIRM("ingredients_update_confirm"),
    INGREDIENTS_UPDATE("ingredients_update"),
    RECIPE_UPDATE_CONFIRM("recipe_update_confirm"),
    RECIPE_UPDATE("recipe_update"),
    FEEDBACK_UPDATE("feedback_update");

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