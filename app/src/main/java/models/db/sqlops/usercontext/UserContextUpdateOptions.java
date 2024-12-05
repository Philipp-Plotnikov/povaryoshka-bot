package models.db.sqlops.usercontext;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import models.commands.CommandStates;

public record UserContextUpdateOptions(
    long userId,
    @NonNull CommandStates commandState,
    @Nullable String dishName
) {}