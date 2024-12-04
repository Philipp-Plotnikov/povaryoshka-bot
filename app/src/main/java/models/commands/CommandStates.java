package models.commands;

import org.checkerframework.checker.nullness.qual.NonNull;


public enum CommandStates {
    DISH_NAME("dish_name"),
    DISH_NAME_UPDATE_CONFIRM("dish_name_update_confirm"),
    NEW_DISH_NAME("new_dish_name"),
    INGREDIENTS_UPDATE_CONFIRM("ingredients_update_confirm"),
    INGREDIENTS("ingredients"),
    RECIPE_UPDATE_CONFIRM("recipe_update_confirm"),
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