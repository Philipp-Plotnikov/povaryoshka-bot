package models.exceptions.db.sqlops;

import org.checkerframework.checker.nullness.qual.NonNull;

public class NotFoundUserContextException extends NotFoundException {
    public NotFoundUserContextException(@NonNull final String message) {
        super(message);
    }
}
