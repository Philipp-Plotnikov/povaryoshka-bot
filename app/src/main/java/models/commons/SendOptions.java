package models.commons;

import org.checkerframework.checker.nullness.qual.NonNull;

public record SendOptions(
    @NonNull Boolean markdown
) {}