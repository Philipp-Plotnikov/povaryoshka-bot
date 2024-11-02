package models.exceptions.db.sqlops;

import org.checkerframework.checker.nullness.qual.NonNull;

public class NotFoundDishException extends NotFoundException {
    public NotFoundDishException(@NonNull final String message) {
        super(message);
    }
}
