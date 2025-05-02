package models.commons;

import org.checkerframework.checker.nullness.qual.Nullable;


public record RequestContext(
    long userId,
    @Nullable String dishName,
    @Nullable String commandType,
    @Nullable String commandState
) {}
