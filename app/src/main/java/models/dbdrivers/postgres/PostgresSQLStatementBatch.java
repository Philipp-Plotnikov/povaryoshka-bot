package models.dbdrivers.postgres; 

import java.sql.SQLException;

@FunctionalInterface
public interface PostgresSQLStatementBatch {
    void execute() throws SQLException, Exception;
}