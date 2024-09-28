package models.sqlops.feedback;

public class FeedbackInsertOptions {
    private final long userId;
    private final String feedback;

    public FeedbackInsertOptions(
        final long userId,
        final String feedback
    ) {
        this.userId = userId;
        this.feedback = feedback;
    }

    public long getUserId() {
        return userId;
    }

    public String getFeedback() {
        return feedback;
    }
}