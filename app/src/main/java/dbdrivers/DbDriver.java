package dbdrivers;

import java.sql.SQLException;

import models.dbdrivers.SQLStatementBatch;
import models.dtos.DishDTO;
import models.dtos.UserContextDTO;
import models.sqlops.dish.DishDeleteOptions;
import models.sqlops.dish.DishInsertOptions;
import models.sqlops.dish.DishSelectOptions;
import models.sqlops.dish.DishUpdateOptions;
import models.sqlops.feedback.FeedbackInsertOptions;
import models.sqlops.usercontext.UserContextDeleteOptions;
import models.sqlops.usercontext.UserContextInsertOptions;
import models.sqlops.usercontext.UserContextSelectOptions;
import models.sqlops.usercontext.UserContextUpdateOptions;

public interface DbDriver extends AutoCloseable {
    public void connect() throws SQLException;
    public void setup() throws SQLException, Exception;
    public void executeAsTransaction(SQLStatementBatch sqlStatementBatch) throws SQLException, Exception;

    public DishDTO selectDish(final DishSelectOptions selectOptions) throws SQLException, Exception;
    public void insertDish(final DishInsertOptions insertOptions) throws SQLException, Exception;
    public void deleteDish(final DishDeleteOptions deleteOptions) throws SQLException;
    public void updateDish(final DishUpdateOptions updateOptions) throws SQLException, Exception;
    
    public UserContextDTO selectUserContext(final UserContextSelectOptions selectOptions) throws SQLException;
    public void insertUserContext(final UserContextInsertOptions insertOptions) throws SQLException;
    public void deleteUserContext(final UserContextDeleteOptions deleteOptions) throws SQLException;
    public void updateUserContext(final UserContextUpdateOptions updateOptions) throws SQLException;
    
    public void insertFeedback(final FeedbackInsertOptions insertOptions) throws SQLException;

    @Override
    public void close() throws SQLException;
}