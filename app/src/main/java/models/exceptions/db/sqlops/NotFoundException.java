package models.exceptions.db.sqlops;

import java.sql.SQLException;

import org.checkerframework.checker.nullness.qual.NonNull;

public class NotFoundException extends SQLException {
    public NotFoundException(@NonNull final String reason) {
        super(reason);
    }
}
