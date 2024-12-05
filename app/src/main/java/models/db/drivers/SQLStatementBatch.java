package models.db.drivers;

@FunctionalInterface
public interface SQLStatementBatch {
    void execute() throws Exception;
}