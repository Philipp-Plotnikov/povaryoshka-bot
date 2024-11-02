package models.db.sqlops.usercontext;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import models.commands.CommandStates;
import models.commands.MultiStateCommandTypes;

public record UserContextInsertOptions(
    long userId,
    @NonNull MultiStateCommandTypes multiStateCommantType,
    @NonNull CommandStates commandState,
    @Nullable String dishName
) {}