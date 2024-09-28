package models.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DishDTO {
    private final String name;
    private final ArrayList<String> ingredientList = new ArrayList();
    private final String recipe;

    // TODO: Replace strings 
    public DishDTO(
        final ResultSet recipeResultSet,
        final ResultSet ingredientResultSet
    ) throws SQLException {
        recipeResultSet.next();
        int columnIndex = recipeResultSet.findColumn("dish_name");
        this.name = recipeResultSet.getString(columnIndex);
        columnIndex = recipeResultSet.findColumn("recipe");
        this.recipe = recipeResultSet.getString(columnIndex);
        while (ingredientResultSet.next()) {
            columnIndex = ingredientResultSet.findColumn("ingredient");
            this.ingredientList.add(ingredientResultSet.getString(columnIndex));
        }
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<String> getIngredientList() {
        return (ArrayList<String>)this.ingredientList.clone();
    }

    public String getRecipe() {
        return this.recipe;
    }
}