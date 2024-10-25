package models.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import models.db.schemas.postgres.PostgresIngredientSchema;
import models.db.schemas.postgres.PostgresRecipeSchema;

public class DishDTO {
    private final String name;
    private final List<String> ingredientList;
    private final String recipe;

    public DishDTO(
        final Statement selectDishStatement
    ) throws SQLException, Exception {
        try (
            final ResultSet recipeResultSet = selectDishStatement.getResultSet();
        ) {
            recipeResultSet.next();
            name = recipeResultSet.getString(PostgresRecipeSchema.DISH_NAME);
            recipe = recipeResultSet.getString(PostgresRecipeSchema.RECIPE);
        }
        if (!selectDishStatement.getMoreResults()) {
            throw new Exception("ingredientResultSet was not found in DishDTO");
        }
        try (
            final ResultSet dishIngredientResultSet = selectDishStatement.getResultSet();
        ) {
            final ArrayList<String> ingredientListBuffer = new ArrayList<>();
            while (dishIngredientResultSet.next()) {
                final String ingredient = dishIngredientResultSet.getString(PostgresIngredientSchema.INGREDIENT);
                ingredientListBuffer.add(ingredient);
            }
            ingredientList = Collections.unmodifiableList(ingredientListBuffer);
        }
    }

    public DishDTO(
        final String dishName,
        final List<String> ingredientList,
        final String recipe
    ) {
        this.name = dishName;
        this.ingredientList = Collections.unmodifiableList(ingredientList);
        this.recipe = recipe;
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