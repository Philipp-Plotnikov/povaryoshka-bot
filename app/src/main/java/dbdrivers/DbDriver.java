package dbdrivers;

import java.sql.SQLException;

import models.dtos.DishDTO;
import models.sqlops.dish.DishDeleteOptions;
import models.sqlops.dish.DishInsertOptions;
import models.sqlops.dish.DishSelectOptions;
import models.sqlops.dish.DishUpdateOptions;
import models.sqlops.feedback.FeedbackInsertOptions;

public interface DbDriver extends AutoCloseable {
    public void connect() throws SQLException;

    public void setup() throws SQLException, Exception;

    public DishDTO selectDish(final DishSelectOptions selectOptions) throws SQLException;

    public void insertDish(final DishInsertOptions insertOptions) throws SQLException, Exception;

    public void deleteDish(final DishDeleteOptions deleteOptions) throws SQLException;

    public void updateDish(final DishUpdateOptions updateOptions) throws SQLException, Exception;

    public void insertFeedback(final FeedbackInsertOptions insertOptions) throws SQLException;

    @Override
    public void close() throws SQLException;
}