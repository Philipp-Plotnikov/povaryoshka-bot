package models.db.sqlops.usercontext;

import models.commands.CommandStates;

public record UserContextUpdateOptions(
    long userId,
    CommandStates commandState,
    String dishName
) {}