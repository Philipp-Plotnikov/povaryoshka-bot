package models.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import models.commands.CommandStates;
import models.commands.MultiStateCommandTypes;
import models.exceptions.db.sqlops.NotFoundUserContextException;

import static models.db.schemas.postgres.PostgresUserContextSchema.COMMAND_STATE;
import static models.db.schemas.postgres.PostgresUserContextSchema.DISH_NAME;
import static models.db.schemas.postgres.PostgresUserContextSchema.MULTI_STATE_COMMAND_TYPE;

public class UserContextDTO {
    @NonNull
    private final MultiStateCommandTypes multiStateCommandType;
    
    @NonNull
    private final CommandStates commandState;
    
    @Nullable
    private final String dishName;

    public UserContextDTO(@NonNull final ResultSet userContextResultSet) throws SQLException,
                                                                                NotFoundUserContextException
    {
        if (!userContextResultSet.next()) {
            throw new NotFoundUserContextException("");
        }
        multiStateCommandType = MultiStateCommandTypes.valueOf(
            userContextResultSet.getString(MULTI_STATE_COMMAND_TYPE).toUpperCase()
        );
        commandState = CommandStates.valueOf(
            userContextResultSet.getString(COMMAND_STATE).toUpperCase()
        );
        dishName = userContextResultSet.getString(DISH_NAME);
    }

    @NonNull
    public MultiStateCommandTypes getMultiStateCommandTypes() {
        return multiStateCommandType;
    }

    @NonNull
    public CommandStates getCommandState() {
        return commandState;
    }

    @Nullable
    public String getDishName() {
        return dishName;
    }
}