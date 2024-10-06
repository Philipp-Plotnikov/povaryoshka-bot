package models.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

import models.CommandStates;
import models.MultiStateCommandTypes;
import static models.schemas.postgres.PostgresUserContextSchema.COMMAND_STATE_INDEX;
import static models.schemas.postgres.PostgresUserContextSchema.MULTI_STATE_COMMAND_TYPE_INDEX;

public class UserContextDTO {
    private final MultiStateCommandTypes multiStateCommandType;
    private final CommandStates commandState;

    public UserContextDTO(final ResultSet userContextResultSet) throws SQLException {
        userContextResultSet.next();
        multiStateCommandType = userContextResultSet.getObject(MULTI_STATE_COMMAND_TYPE_INDEX, MultiStateCommandTypes.class);
        commandState = userContextResultSet.getObject(COMMAND_STATE_INDEX, CommandStates.class);
    }

    public MultiStateCommandTypes getMultiStateCommandTypes() {
        return multiStateCommandType;
    }

    public CommandStates getCommandState() {
        return commandState;
    }
}