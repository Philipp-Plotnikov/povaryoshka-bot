package models.commons;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public record RequestContext(
    @NonNull long userId,
    @Nullable String dishName,
    @Nullable String commandType,
    @Nullable String commandState
) {}
