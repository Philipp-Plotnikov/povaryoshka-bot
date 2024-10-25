package dbdrivers;

import java.sql.SQLException;
import java.util.List;

import models.db.drivers.SQLStatementBatch;
import models.db.sqlops.dish.DishDeleteOptions;
import models.db.sqlops.dish.DishInsertOptions;
import models.db.sqlops.dish.DishListSelectOptions;
import models.db.sqlops.dish.DishSelectOptions;
import models.db.sqlops.dish.DishUpdateOptions;
import models.db.sqlops.feedback.FeedbackInsertOptions;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import models.db.sqlops.usercontext.UserContextInsertOptions;
import models.db.sqlops.usercontext.UserContextSelectOptions;
import models.db.sqlops.usercontext.UserContextUpdateOptions;
import models.dtos.DishDTO;
import models.dtos.UserContextDTO;

// TODO: Specify all exceptin types or one generic if I can ?
public interface DbDriver extends AutoCloseable {
    public void connect() throws SQLException;
    public void setup() throws SQLException, Exception;
    public void executeAsTransaction(SQLStatementBatch sqlStatementBatch) throws SQLException, Exception;

    public DishDTO selectDish(final DishSelectOptions selectOptions) throws SQLException, Exception;
    public List<DishDTO> selectDishList(final DishListSelectOptions selectOptions) throws SQLException;
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