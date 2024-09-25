package models.sqlops;

public class DeleteOptions {
    private final long userId;
    private final String dishName;

    public DeleteOptions(
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