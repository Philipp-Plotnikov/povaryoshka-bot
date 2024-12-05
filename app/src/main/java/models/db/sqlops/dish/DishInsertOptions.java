package models.db.sqlops.dish;

import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public record DishInsertOptions(
    long userId,
    @NonNull String dishName,
    @Nullable List<String> ingredientList,
    @Nullable String recipe
) {
    public DishInsertOptions {
        ingredientList = ingredientList == null
                ? ingredientList
                : Collections.unmodifiableList(ingredientList);
    }
}