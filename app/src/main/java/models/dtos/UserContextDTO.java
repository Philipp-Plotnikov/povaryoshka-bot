package models.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

import models.commands.CommandStates;
import models.commands.MultiStateCommandTypes;

import static models.db.schemas.postgres.PostgresUserContextSchema.*;

public class UserContextDTO {
    private final MultiStateCommandTypes multiStateCommandType;
    private final CommandStates commandState;

    public UserContextDTO(final ResultSet userContextResultSet) throws SQLException {
        userContextResultSet.next();
        multiStateCommandType = MultiStateCommandTypes.valueOf(
                userContextResultSet.getString(MULTI_STATE_COMMAND_TYPE).toUpperCase()
        );
        commandState = CommandStates.valueOf(
                userContextResultSet.getString(COMMAND_STATE).toUpperCase()
        );
    }

    public MultiStateCommandTypes getMultiStateCommandTypes() {
        return multiStateCommandType;
    }

    public CommandStates getCommandState() {
        return commandState;
    }
}