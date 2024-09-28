package models.dbdrivers.postgres;

public enum PostgresConnectionProperties {
    USER("user"),
    PASSWORD("password"),
    CURRENT_SCHEMA("currentSchema");

    private final String value;

    PostgresConnectionProperties(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}