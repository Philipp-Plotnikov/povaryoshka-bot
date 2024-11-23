package models.db.sqlops.dish;

import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public record DishUpdateOptions(
    long userId,
    @NonNull String dishName,
    @Nullable List<String> ingredientList,
    @Nullable String recipe
) {
    public DishUpdateOptions {
        ingredientList = ingredientList == null
                ? ingredientList
                : Collections.unmodifiableList(ingredientList);
    }
}