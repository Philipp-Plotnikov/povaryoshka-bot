package models.db.sqlops.usercontext;

import models.commands.CommandStates;
import models.commands.MultiStateCommandTypes;

public record UserContextInsertOptions(
    long userId,
    MultiStateCommandTypes multiStateCommantType,
    CommandStates commandState
) {}