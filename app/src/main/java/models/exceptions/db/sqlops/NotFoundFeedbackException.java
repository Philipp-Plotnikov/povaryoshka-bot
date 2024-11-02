package models.exceptions.db.sqlops;

import org.checkerframework.checker.nullness.qual.NonNull;

public class NotFoundFeedbackException extends NotFoundException {
    public NotFoundFeedbackException(@NonNull final String message) {
        super(message);
    }
}
