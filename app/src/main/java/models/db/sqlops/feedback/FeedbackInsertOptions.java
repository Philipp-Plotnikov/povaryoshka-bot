package models.db.sqlops.feedback;

public record FeedbackInsertOptions(
    long userId,
    String feedback
) {}