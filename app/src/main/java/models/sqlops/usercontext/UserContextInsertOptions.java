package models.sqlops.usercontext;

import models.CommandStates;
import models.MultiStateCommandTypes;

public record UserContextInsertOptions(
    long userId,
    MultiStateCommandTypes multiStateCommantType,
    CommandStates commandState
) {}