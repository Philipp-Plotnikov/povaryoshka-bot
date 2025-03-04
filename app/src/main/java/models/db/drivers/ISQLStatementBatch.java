package models.db.drivers;

@FunctionalInterface
public interface ISQLStatementBatch {
    void execute() throws Exception;
}