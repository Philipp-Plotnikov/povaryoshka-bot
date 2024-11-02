package models.db.sqlops.dish;

import org.jetbrains.annotations.NotNull;

public record DishSelectOptions(
    long userId,
    @NotNull String dishName
) {}