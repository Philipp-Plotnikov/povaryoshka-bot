package models.sqlops.dish;

public record DishDeleteOptions(
    long userId,
    String dishName
) {}