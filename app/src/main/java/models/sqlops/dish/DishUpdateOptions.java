package models.sqlops.dish;

import java.util.Collections;
import java.util.List;

public record DishUpdateOptions(
    long userId,
    String dishName,
    List<String> ingredientList,
    String recipe
) {
    public DishUpdateOptions {
        ingredientList = Collections.unmodifiableList(ingredientList);
    }
}