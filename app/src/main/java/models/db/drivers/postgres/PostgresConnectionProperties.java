package models.db.drivers.postgres;

import org.checkerframework.checker.nullness.qual.NonNull;

public class PostgresConnectionProperties {
    @NonNull
    public static final String USER = "user";

    @NonNull
    public static final String PASSWORD = "password";

    @NonNull
    public static final String CURRENT_SCHEMA = "currentSchema";
}