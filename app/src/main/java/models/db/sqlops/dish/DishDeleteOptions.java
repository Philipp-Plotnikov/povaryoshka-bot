package models.db.sqlops.dish;

public record DishDeleteOptions(
    long userId,
    String dishName
) {}