package models.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

import models.commands.CommandStates;
import models.commands.MultiStateCommandTypes;
import static models.db.schemas.postgres.PostgresUserContextSchema.COMMAND_STATE;
import static models.db.schemas.postgres.PostgresUserContextSchema.DISH_NAME;
import static models.db.schemas.postgres.PostgresUserContextSchema.MULTI_STATE_COMMAND_TYPE;

public class UserContextDTO {
    private final MultiStateCommandTypes multiStateCommandType;
    private final CommandStates commandState;
    private final String dishName;

    // TODO: Think about getObject
    public UserContextDTO(final ResultSet userContextResultSet) throws SQLException {
        userContextResultSet.next();
        multiStateCommandType = MultiStateCommandTypes.valueOf(
            userContextResultSet.getString(MULTI_STATE_COMMAND_TYPE).toUpperCase()
        );
        commandState = CommandStates.valueOf(
            userContextResultSet.getString(COMMAND_STATE).toUpperCase()
        );
        dishName = userContextResultSet.getString(DISH_NAME);
    }

    public MultiStateCommandTypes getMultiStateCommandTypes() {
        return multiStateCommandType;
    }

    public CommandStates getCommandState() {
        return commandState;
    }

    public String getDishName() {
        return dishName;
    }
}