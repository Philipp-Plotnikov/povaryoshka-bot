package models.sqlops.dish;

public class DishSelectOptions {
    private final long userId;
    private final String dishName;

    public DishSelectOptions(
        final long userId,
        final String dishName
    ) {
        this.userId = userId;
        this.dishName = dishName;
    }

    public long getUserId() {
        return this.userId;
    }

    public String getDishName() {
        return this.dishName;
    }
}