package models.db.sqlops.feedback;

import org.checkerframework.checker.nullness.qual.NonNull;

public record FeedbackInsertOptions(
    long userId,
    @NonNull String feedback
) {}