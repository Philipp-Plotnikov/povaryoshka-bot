package models.db.sqlops.dish;

import org.checkerframework.checker.nullness.qual.NonNull;

public record DishDeleteOptions(
    long userId,
    @NonNull String dishName
) {}