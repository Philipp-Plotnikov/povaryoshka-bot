package models.commons;

import org.checkerframework.checker.nullness.qual.NonNull;

public record SendCommandOption(
    @NonNull Boolean markdown
) {}
