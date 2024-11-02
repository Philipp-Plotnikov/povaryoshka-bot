package models.commands;

import org.checkerframework.checker.nullness.qual.NonNull;

public record CommandSettings(
    @NonNull String commandName,
    @NonNull String commandDescription
) {}