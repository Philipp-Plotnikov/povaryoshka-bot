package models.sqlops.usercontext;

import models.CommandStates;

public record UserContextUpdateOptions(
    long userId,
    CommandStates commandState
) {}