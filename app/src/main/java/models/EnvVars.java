package models;

public enum EnvVars {
    DB_HOST("DB_HOST"),
    DB_PORT("DB_PORT"),
    DB_DATABASE("DB_DATABASE"),
    DB_SCHEMA("DB_SCHEMA"),
    DB_USERNAME("DB_USERNAME"),
    DB_PASSWORD("DB_PASSWORD");

    private final String value;

    EnvVars(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}