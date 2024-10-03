package models.sqlops.feedback;

public record FeedbackInsertOptions(
    long userId,
    String feedback
) {}