package models.sqlops.dish;

import java.util.ArrayList;

public class DishUpdateOptions {
    private final long userId;
    private final String dishName;
    private final ArrayList<String> ingredientList;
    private final String recipe;

    public DishUpdateOptions(
        final long userId,
        final String dishName,
        final ArrayList<String> ingredientList,
        final String recipe
    ) {
        this.userId = userId;
        this.dishName = dishName;
        this.ingredientList = ingredientList;
        this.recipe = recipe;
    }

    public long getUserId() {
        return this.userId;
    }

    public String getDishName() {
        return this.dishName;
    }

    public ArrayList<String> getIngredientList() {
        return (ArrayList<String>)this.ingredientList.clone();
    }

    public String getRecipe() {
        return this.recipe;
    }
}