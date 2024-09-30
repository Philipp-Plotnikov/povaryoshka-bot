package models.sqlops.dish;

import java.util.Collections;
import java.util.List;

public class DishUpdateOptions {
    private final long userId;
    private final String dishName;
    private final List<String> ingredientList;
    private final String recipe;

    public DishUpdateOptions(
        final long userId,
        final String dishName,
        final List<String> ingredientList,
        final String recipe
    ) {
        this.userId = userId;
        this.dishName = dishName;
        this.ingredientList = Collections.unmodifiableList(ingredientList);
        this.recipe = recipe;
    }

    public long getUserId() {
        return userId;
    }

    public String getDishName() {
        return dishName;
    }

    public List<String> getIngredientList() {
        return ingredientList;
    }

    public String getRecipe() {
        return recipe;
    }
}