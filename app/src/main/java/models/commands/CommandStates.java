package models.commands;

import org.checkerframework.checker.nullness.qual.NonNull;

public enum CommandStates {
    DISH_NAME("dish_name"),
    IS_INGREDIENTS_UPDATE("is_ingredients_update"),
    CONFIRM_INGREDIENT_UPDATE("confirm_ingredient_update"),
    INGREDIENTS("ingredients"),
    IS_RECIPE_UPDATE("is_recipe_update"),
    CONFIRM_RECIPE_UPDATE("confirm_recipe_update"),
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