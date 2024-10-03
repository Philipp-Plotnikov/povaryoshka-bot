package models.sqlops.dish;

import java.util.Collections;
import java.util.List;

public record DishInsertOptions(
    long userId,
    String dishName,
    List<String> ingredientList,
    String recipe
) {
    public DishInsertOptions {
        ingredientList = Collections.unmodifiableList(ingredientList);
    }
}