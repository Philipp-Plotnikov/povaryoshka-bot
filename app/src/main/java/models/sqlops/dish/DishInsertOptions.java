package models.sqlops.dish;

import java.util.ArrayList;

public class DishInsertOptions {
    private final long userId;
    private final String dishName;
    private final ArrayList<String> ingredientList;
    private final String recipe;

    public DishInsertOptions(
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

    public ArrayList<String> getIngredientList() throws Exception {
        Object ingredientListObject = this.ingredientList.clone();
        if (ingredientListObject instanceof ArrayList<?> untypedIngredientList) {
            ArrayList<String> ingredientListCopy = new ArrayList<>();
            for (Object ingredientObject : untypedIngredientList) {
                if (ingredientObject instanceof String ingredient) {
                    ingredientListCopy.add(ingredient);
                    continue;
                }
                throw new Exception();
            }
            return ingredientListCopy;
        }
        throw new Exception();
    }

    public String getRecipe() {
        return this.recipe;
    }
}