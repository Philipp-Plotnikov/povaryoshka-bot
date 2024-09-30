package models.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import models.schemas.postgres.PostgresIngredientSchema;
import models.schemas.postgres.PostgresRecipeSchema;

public class DishDTO {
    private final String name;
    private final List<String> ingredientList;
    private final String recipe;

    public DishDTO(
        final ResultSet recipeResultSet,
        final ResultSet ingredientResultSet
    ) throws SQLException {
        recipeResultSet.next();
        name = recipeResultSet.getString(PostgresRecipeSchema.DISH_NAME_INDEX);
        recipe = recipeResultSet.getString(PostgresRecipeSchema.RECIPE_INDEX);
        final ArrayList<String> ingredientListBuffer = new ArrayList<>();
        while (ingredientResultSet.next()) {
            final String ingredient = ingredientResultSet.getString(PostgresIngredientSchema.INGREDIENT_INDEX);
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