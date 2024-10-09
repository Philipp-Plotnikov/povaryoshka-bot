package models.db.schemas.postgres;

public class PostgresUserContextSchema {
    public static final String USER_ID = "user_id";
    public static final String MULTI_STATE_COMMAND_TYPE = "multi_state_command_type";
    public static final String COMMAND_STATE = "command_state";

    public static final int USER_ID_INDEX = 1;
    public static final int MULTI_STATE_COMMAND_TYPE_INDEX = 2;
    public static final int COMMAND_STATE_INDEX = 3;
}
