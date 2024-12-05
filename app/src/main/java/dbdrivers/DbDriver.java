package dbdrivers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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

public interface DbDriver extends AutoCloseable {    
    public void setup() throws SQLException, Exception;
    
    public void executeAsTransaction(@NonNull SQLStatementBatch sqlStatementBatch) throws SQLException, Exception;


    @Nullable
    public DishDTO selectDish(@NonNull final DishSelectOptions selectOptions) throws SQLException;    
    
    @Nullable
    public List<DishDTO> selectDishList(@NonNull final DishListSelectOptions selectOptions) throws SQLException;

    public void insertDish(@NonNull final DishInsertOptions insertOptions) throws SQLException, Exception;
    
    public void deleteDish(@NonNull final DishDeleteOptions deleteOptions) throws SQLException;
    
    public void updateDish(@NonNull final DishUpdateOptions updateOptions) throws SQLException, Exception;
    
    public void updateDishName(@NonNull final DishUpdateOptions updateOptions) throws SQLException, Exception;

    public void updateDishIngredientList(@NonNull final DishUpdateOptions updateOptions) throws SQLException, Exception;
    
    public void updateDishRecipe(@NonNull final DishUpdateOptions updateOptions) throws SQLException;
    

    @Nullable
    public UserContextDTO selectUserContext(@NonNull final UserContextSelectOptions selectOptions) throws SQLException;
    
    public void insertUserContext(@NonNull final UserContextInsertOptions insertOptions) throws SQLException;
    
    public void deleteUserContext(@NonNull final UserContextDeleteOptions deleteOptions) throws SQLException;
    
    public void updateUserContext(@NonNull final UserContextUpdateOptions updateOptions) throws SQLException;
    
    public void updateUserContextCommandState(@NonNull final UserContextUpdateOptions updateOptions) throws SQLException;

    public void insertFeedback(@NonNull final FeedbackInsertOptions insertOptions) throws SQLException;

    
    @Override
    public void close() throws SQLException, IOException;
}