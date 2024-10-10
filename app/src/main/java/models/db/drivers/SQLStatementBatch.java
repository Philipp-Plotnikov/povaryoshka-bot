package models.db.drivers;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLStatementBatch {
    void execute() throws SQLException, Exception;
}