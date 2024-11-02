package models.commands;

import org.checkerframework.checker.nullness.qual.NonNull;

public enum MultiStateCommandTypes {
    CREATE("create"),
    GET("get"),
    UPDATE("update"),
    DELETE("delete"),
    FEEDBACK("feedback"),
    HELP("help");

    @NonNull
    private final String value;

    private MultiStateCommandTypes(@NonNull final String value) {
        this.value = value;
    }

    @NonNull
    public String getValue() {
        return value;
    }
}