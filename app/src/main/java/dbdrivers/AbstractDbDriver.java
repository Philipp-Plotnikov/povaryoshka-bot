package dbdrivers;

import java.sql.SQLException;

import models.Dish;
import models.sqlops.DeleteOptions;
import models.sqlops.InsertOptions;
import models.sqlops.SelectOptions;
import models.sqlops.UpdateOptions;

public abstract class AbstractDbDriver implements AutoCloseable {
    public abstract void connect() throws SQLException;

    public abstract void setup() throws Exception;

    public abstract Dish selectDish(final SelectOptions selectOptions) throws SQLException;

    public abstract void insertDish(final InsertOptions insertOptions) throws SQLException;

    public abstract void deleteDish(final DeleteOptions deleteOptions) throws SQLException;

    public abstract void updateDish(final UpdateOptions updateOptions) throws SQLException;
 
    @Override
    public abstract void close() throws SQLException;
}