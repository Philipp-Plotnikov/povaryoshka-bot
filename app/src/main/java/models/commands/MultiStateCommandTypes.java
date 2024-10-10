package models.commands;

public enum MultiStateCommandTypes {
    CREATE("create"),
    GET("get"),
    UPDATE("update"),
    DELETE("delete"),
    FEEDBACK("feedback"),
    HELP("help");

    private final String value;

    private MultiStateCommandTypes(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}