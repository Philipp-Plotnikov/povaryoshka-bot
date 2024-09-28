package dbdrivers;

import java.sql.SQLException;

import models.dtos.DishDTO;
import models.sqlops.dish.DishDeleteOptions;
import models.sqlops.dish.DishInsertOptions;
import models.sqlops.dish.DishSelectOptions;
import models.sqlops.dish.DishUpdateOptions;
import models.sqlops.feedback.FeedbackInsertOptions;

public abstract class AbstractDbDriver implements AutoCloseable {
    public abstract void connect() throws SQLException;

    public abstract void setup() throws Exception;

    public abstract DishDTO selectDish(final DishSelectOptions selectOptions) throws SQLException;

    public abstract void insertDish(final DishInsertOptions insertOptions) throws SQLException, Exception;

    public abstract void deleteDish(final DishDeleteOptions deleteOptions) throws SQLException;

    public abstract void updateDish(final DishUpdateOptions updateOptions) throws SQLException, Exception;

    public abstract void insertFeedback(final FeedbackInsertOptions insertOptions) throws SQLException;
 
    @Override
    public abstract void close() throws SQLException;
}