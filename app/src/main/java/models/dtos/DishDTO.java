package models.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static models.schemas.postgres.PostgresIngredientSchema.DISH_NAME_INDEX;
import static models.schemas.postgres.PostgresIngredientSchema.INGREDIENT_INDEX;
import static models.schemas.postgres.PostgresRecipeSchema.RECIPE_INDEX;

public class DishDTO {
    private final String name;
    private final List<String> ingredientList;
    private final String recipe;

    public DishDTO(
        final ResultSet recipeResultSet,
        final ResultSet ingredientResultSet
    ) throws SQLException {
        recipeResultSet.next();
        name = recipeResultSet.getString(DISH_NAME_INDEX);
        recipe = recipeResultSet.getString(RECIPE_INDEX);
        final ArrayList<String> ingredientListBuffer = new ArrayList<>();
        while (ingredientResultSet.next()) {
            final String ingredient = ingredientResultSet.getString(INGREDIENT_INDEX);
            ingredientListBuffer.add(ingredient);
        }
        ingredientList = Collections.unmodifiableList(ingredientListBuffer);
    }

    public String getName() {
        return name;
    }

    public List<String> getIngredientList() {
        return ingredientList;
    }

    public String getRecipe() {
        return recipe;
    }
}