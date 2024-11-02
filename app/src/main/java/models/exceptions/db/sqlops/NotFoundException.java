package models.exceptions.db.sqlops;

import org.checkerframework.checker.nullness.qual.NonNull;

public class NotFoundException extends Exception {
    public NotFoundException(@NonNull final String message) {
        super(message);
    }
}
