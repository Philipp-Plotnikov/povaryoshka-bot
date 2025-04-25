package models.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import models.db.schemas.postgres.PostgresIngredientSchema;
import models.db.schemas.postgres.PostgresRecipeSchema;
import static models.exceptions.db.sqlops.ExceptionMessages.INGREDIENT_RESULT_SET_NOT_FOUND;
import static models.exceptions.db.sqlops.ExceptionMessages.RECIPE_NOT_FOUND;
import models.exceptions.db.sqlops.NotFoundDishException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DishDTO {
    @NonNull
    private final static Logger logger = LoggerFactory.getLogger(DishDTO.class);

    @NonNull
    private final String name;

    @Nullable
    private final List<String> ingredientList;

    @Nullable
    private final String recipe;

    public DishDTO(@NonNull final Statement selectDishStatement) throws SQLException,
                                                                        NotFoundDishException
    {
        try (
            final ResultSet recipeResultSet = selectDishStatement.getResultSet();
        ) {
            if (!recipeResultSet.next()) {
                logger.error(RECIPE_NOT_FOUND);
                throw new NotFoundDishException(RECIPE_NOT_FOUND);
            }
            name = recipeResultSet.getString(PostgresRecipeSchema.DISH_NAME);
            recipe = recipeResultSet.getString(PostgresRecipeSchema.RECIPE);
        }
        if (!selectDishStatement.getMoreResults()) {
            logger.error(INGREDIENT_RESULT_SET_NOT_FOUND);
            throw new NotFoundDishException(INGREDIENT_RESULT_SET_NOT_FOUND);
        }
        try (
            final ResultSet dishIngredientResultSet = selectDishStatement.getResultSet();
        ) {
            final ArrayList<String> ingredientListBuffer = new ArrayList<>();
            while (dishIngredientResultSet.next()) {
                final String ingredient = dishIngredientResultSet.getString(PostgresIngredientSchema.INGREDIENT);
                ingredientListBuffer.add(ingredient);
            }
            if (ingredientListBuffer.isEmpty()) {
                ingredientList = null;
                return;
            }
            ingredientList = Collections.unmodifiableList(ingredientListBuffer);
            logger.debug("DishDTO has been initialized");
        }
    }

    public DishDTO(
        @NonNull final String dishName,
        @Nullable final List<String> ingredientList,
        @Nullable final String recipe
    ) {
        this.name = dishName;
        this.ingredientList = ingredientList == null
                ? ingredientList
                : Collections.unmodifiableList(ingredientList);
        this.recipe = recipe;
        logger.debug("DishDTO has been initialized");
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public List<String> getIngredientList() {
        return ingredientList;
    }

    @Nullable
    public String getRecipe() {
        return recipe;
    }
}