package models.db.sqlops.usercontext;

import org.checkerframework.checker.nullness.qual.Nullable;

import models.commands.CommandStates;
import models.commands.MultiStateCommandTypes;

public record UserContextInsertOptions(
    long userId,
    MultiStateCommandTypes multiStateCommantType,
    CommandStates commandState,
    @Nullable String dishName
) {}