package models.replies;

import org.checkerframework.checker.nullness.qual.NonNull;


public record ReplySettings(
        @NonNull String replyName,
        @NonNull String replyDescription
) {}