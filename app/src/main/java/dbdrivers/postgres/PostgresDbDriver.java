package dbdrivers.postgres;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import dbdrivers.IDbDriver;
import models.db.drivers.ISQLStatementBatch;
import static models.db.drivers.postgres.PostgresConnectionProperties.CURRENT_SCHEMA;
import static models.db.drivers.postgres.PostgresConnectionProperties.PASSWORD;
import static models.db.drivers.postgres.PostgresConnectionProperties.USER;
import models.db.drivers.postgres.PostgresDbDriverOptions;
import models.db.schemas.postgres.PostgresFeedbackSchema;
import models.db.schemas.postgres.PostgresIngredientSchema;
import models.db.schemas.postgres.PostgresRecipeSchema;
import models.db.schemas.postgres.PostgresUserContextSchema;
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
import models.exceptions.db.sqlops.NotFoundDishException;
import models.exceptions.db.sqlops.NotFoundUserContextException;


final public class PostgresDbDriver implements IDbDriver {
    @NonNull
    private final PostgresDbDriverOptions postgresDbDriverOptions;

    private Connection connection;

    public PostgresDbDriver(@NonNull final PostgresDbDriverOptions options) throws SQLException
    {
        postgresDbDriverOptions = options;
        connect();
    }

    private void connect() throws SQLException
    {
        Properties connectionProperties = new Properties();
        setConnectionProperties(connectionProperties);
        connection = DriverManager.getConnection(postgresDbDriverOptions.getDbUrl(), connectionProperties);
    }

    private void setConnectionProperties(@NonNull final Properties connectionProperties)
    {
        connectionProperties.setProperty(USER, postgresDbDriverOptions.getDbUsername());
        connectionProperties.setProperty(PASSWORD, postgresDbDriverOptions.getDbPassword());
        connectionProperties.setProperty(CURRENT_SCHEMA, postgresDbDriverOptions.getDbSchema());
    }

    @Override
    public void setup() throws SQLException, Exception
    {
        executeAsTransaction(() -> {
            runInitScripts();
            runAlterScripts();
        });
    }

    @Override
    public void executeAsTransaction(@NonNull ISQLStatementBatch sqlStatementBatch) throws SQLException,
                                                                                          Exception
    {
        if (postgresDbDriverOptions.getIsDistributedDatabase()) {
            throw new Exception("Distributed database is not supported yet");
        }
        executeAsOnePhaseTransaction(sqlStatementBatch);
    }

    private void executeAsOnePhaseTransaction(@NonNull ISQLStatementBatch sqlStatementBatch) throws SQLException,
                                                                                                   Exception
    {
        boolean currentAutoCommitState = connection.getAutoCommit();
        if (!currentAutoCommitState) {
            sqlStatementBatch.execute();
            return;
        }
        try {
            connection.setAutoCommit(false);
            sqlStatementBatch.execute();
            connection.commit();
        } catch(Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    @Nullable
    public DishDTO selectDish(@NonNull final DishSelectOptions selectOptions) throws SQLException
    {
        DishDTO dishDTO;
        try (
            final Statement selectDishStatement = connection.createStatement();
            final PreparedStatement selectRecipePreparedStatement = getSelectPreparedRecipeStatement(selectOptions);
            final PreparedStatement selectDishIngredientListPreparedStatement = getSelectPreparedDishIngredientListStatement(selectOptions);
        ) {
            selectDishStatement.execute(
                String.format(
                    "%s; %s",
                    selectRecipePreparedStatement.toString(),
                    selectDishIngredientListPreparedStatement.toString()
                )
            );      
            dishDTO = new DishDTO(selectDishStatement);
        } catch (NotFoundDishException e) {
            dishDTO = null;
        }
        return dishDTO;
    }

    @NonNull
    private PreparedStatement getSelectPreparedRecipeStatement(@NonNull final DishSelectOptions selectOptions) throws SQLException
    {  
        final String recipeSelect = String.format(
            "SELECT dish_name, recipe FROM %s.recipe WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectRecipePreparedStatement = connection.prepareStatement(recipeSelect);
        selectRecipePreparedStatement.setLong(1, selectOptions.userId());
        selectRecipePreparedStatement.setString(2, selectOptions.dishName());
        return selectRecipePreparedStatement;
    }

    @NonNull
    private PreparedStatement getSelectPreparedDishIngredientListStatement(@NonNull final DishSelectOptions selectOptions) throws SQLException
    { 
        final String dishIngredientListSelect = String.format(
            "SELECT ingredient FROM %s.ingredient WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectDishIngredientListPreparedStatement = connection.prepareStatement(dishIngredientListSelect);
        selectDishIngredientListPreparedStatement.setLong(1, selectOptions.userId());
        selectDishIngredientListPreparedStatement.setString(2, selectOptions.dishName());
        return selectDishIngredientListPreparedStatement;
    }

    @Override
    @Nullable
    public List<DishDTO> selectDishList(@NonNull final DishListSelectOptions selectOptions) throws SQLException
    {
        final ArrayList<DishDTO> dishList = new ArrayList<>();
        try (
            final PreparedStatement selectRecipeListPreparedStatement = getSelectPreparedRecipeListStatement(selectOptions);
            final PreparedStatement selectIngredientListPreparedStatement = getSelectPreparedIngredientListStatement(selectOptions);
            final ResultSet recipeListResultSet = selectRecipeListPreparedStatement.executeQuery();
        ) {
            String dishName, recipe;
            List<String> ingredientList;
            while (recipeListResultSet.next()) {
                dishName = recipeListResultSet.getString(PostgresRecipeSchema.DISH_NAME);
                recipe = recipeListResultSet.getString(PostgresRecipeSchema.RECIPE);
                selectIngredientListPreparedStatement.setString(2, dishName);
                ingredientList = getDishIngredientList(selectIngredientListPreparedStatement);
                dishList.add(new DishDTO(
                    dishName,
                    ingredientList,
                    recipe
                ));
            }
        }
        if (dishList.size() == 0) {
            return null;
        }
        return Collections.unmodifiableList(dishList);
    }

    @Nullable
    private List<String> getDishIngredientList(@NonNull final PreparedStatement selectIngredientListPreparedStatement) throws SQLException
    {
        final List<String> ingredientList = new ArrayList<>();
        try (
            final ResultSet dishIngredientResultSet = selectIngredientListPreparedStatement.executeQuery();
        ) {
            while (dishIngredientResultSet.next()) {
                final String ingredient = dishIngredientResultSet.getString(PostgresIngredientSchema.INGREDIENT);
                ingredientList.add(ingredient);
            }
        }
        if (ingredientList.size() == 0) {
            return null;
        }
        return Collections.unmodifiableList(ingredientList);
    }

    @NonNull
    private PreparedStatement getSelectPreparedRecipeListStatement(@NonNull final DishListSelectOptions selectOptions) throws SQLException
    {
        final String recipeSelect = String.format(
            "SELECT dish_name, recipe FROM %s.recipe WHERE user_id = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectRecipePreparedStatement = connection.prepareStatement(recipeSelect);
        selectRecipePreparedStatement.setLong(1, selectOptions.userId());
        return selectRecipePreparedStatement;
    }

    @NonNull
    private PreparedStatement getSelectPreparedIngredientListStatement(@NonNull final DishListSelectOptions selectOptions) throws SQLException
    {
        final String ingredientListSelect = String.format(
            "SELECT ingredient FROM %s.ingredient WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectIngredientListPreparedStatement = connection.prepareStatement(ingredientListSelect);
        selectIngredientListPreparedStatement.setLong(1, selectOptions.userId());
        return selectIngredientListPreparedStatement;
    }

    @Override
    public void insertDish(@NonNull final DishInsertOptions insertOptions) throws SQLException, Exception
    {
        executeAsTransaction(() -> {
            internalInsertDish(insertOptions);
        });
    }

    private void internalInsertDish(@NonNull final DishInsertOptions insertOptions) throws SQLException
    { 
        try (
            final Statement dishStatement = connection.createStatement();
            final PreparedStatement insertRecipePreparedStatement = getInsertPreparedRecipeStatement(insertOptions);
            final PreparedStatement insertIngredientPreparedStatement = getInsertPreparedIngredientStatement(insertOptions);
        ) {
            dishStatement.addBatch(insertRecipePreparedStatement.toString());
            final List<String> ingredientList = insertOptions.ingredientList();
            if (ingredientList != null) {
                for (String ingredient : ingredientList) {
                    insertIngredientPreparedStatement.setString(3, ingredient);
                    dishStatement.addBatch(insertIngredientPreparedStatement.toString());
                }
            }
            dishStatement.executeBatch();
        }
    }

    @NonNull
    private PreparedStatement getInsertPreparedRecipeStatement(@NonNull final DishInsertOptions insertOptions) throws SQLException
    {
        final String recipeInsert = String.format(
            "INSERT INTO %s.recipe (%s, %s, %s) VALUES (?, ?, ?);",
            postgresDbDriverOptions.getDbSchema(),
            PostgresRecipeSchema.USER_ID,
            PostgresRecipeSchema.DISH_NAME,
            PostgresRecipeSchema.RECIPE
        );
        final PreparedStatement insertRecipePreparedStatement = connection.prepareStatement(recipeInsert);
        insertRecipePreparedStatement.setLong(1, insertOptions.userId());
        insertRecipePreparedStatement.setString(2, insertOptions.dishName());
        insertRecipePreparedStatement.setString(3, insertOptions.recipe());
        return insertRecipePreparedStatement;
    }

    @NonNull
    private PreparedStatement getInsertPreparedIngredientStatement(@NonNull final DishInsertOptions insertOptions) throws SQLException
    {
        final String ingredientInsert = String.format(
            "INSERT INTO %s.ingredient (%s, %s, %s) VALUES (?, ?, ?);",
            postgresDbDriverOptions.getDbSchema(),
            PostgresIngredientSchema.USER_ID,
            PostgresIngredientSchema.DISH_NAME,
            PostgresIngredientSchema.INGREDIENT
        );
        final PreparedStatement insertIngredientPreparedStatement = connection.prepareStatement(ingredientInsert);
        insertIngredientPreparedStatement.setLong(1, insertOptions.userId());
        insertIngredientPreparedStatement.setString(2, insertOptions.dishName());
        return insertIngredientPreparedStatement;
    }

    @Override
    public void deleteDish(@NonNull final DishDeleteOptions deleteOptions) throws SQLException
    {
        deleteRecipe(deleteOptions);
    }

    private void deleteRecipe(@NonNull final DishDeleteOptions deleteOptions) throws SQLException
    {
        try (
            final PreparedStatement deleteRecipePreparedStatement = getDeletePreparedRecipeStatement(deleteOptions);
        ) {
            int deletedRowAmount = deleteRecipePreparedStatement.executeUpdate();
            if (deletedRowAmount == 0) {
                throw new NotFoundDishException("");
            }
        }
    }

    @NonNull
    private PreparedStatement getDeletePreparedRecipeStatement(@NonNull final DishDeleteOptions deleteOptions) throws SQLException
    {
        final String recipeDelete = String.format(
            "DELETE FROM %s.recipe WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement deleteRecipePreparedStatement = connection.prepareStatement(recipeDelete);
        deleteRecipePreparedStatement.setLong(1, deleteOptions.userId());
        deleteRecipePreparedStatement.setString(2, deleteOptions.dishName());
        return deleteRecipePreparedStatement;
    }

    @Override
    public void updateDish(@NonNull final DishUpdateOptions updateOptions) throws SQLException, Exception
    {
        executeAsTransaction(() -> {
            internalUpdateDish(updateOptions);
        });
    }

    private void internalUpdateDish(@NonNull final DishUpdateOptions updateOptions) throws SQLException
    {
        final DishInsertOptions insertOptions = new DishInsertOptions(
            updateOptions.userId(),
            updateOptions.dishName(),
            updateOptions.ingredientList(),
            updateOptions.recipe()
        );
        try (
            final Statement dishStatement = connection.createStatement();
            final PreparedStatement updateRecipePreparedStatement = getUpdatePreparedRecipeStatement(updateOptions);
            final PreparedStatement deleteIngredientPreparedStatement = getDeletePreparedIngredientStatement(updateOptions);
            final PreparedStatement insertIngredientPreparedStatement = getInsertPreparedIngredientStatement(insertOptions);
        ) {
            dishStatement.addBatch(updateRecipePreparedStatement.toString());
            dishStatement.addBatch(deleteIngredientPreparedStatement.toString());
            final List<String> ingredientList = insertOptions.ingredientList();
            if (ingredientList != null) {
                for (String ingredient : ingredientList) {
                    insertIngredientPreparedStatement.setString(3, ingredient);
                    dishStatement.addBatch(insertIngredientPreparedStatement.toString());
                }
            }
            dishStatement.executeBatch();
        }
    }

    @NonNull
    private PreparedStatement getUpdatePreparedRecipeStatement(@NonNull final DishUpdateOptions updateOptions) throws SQLException
    {
        final String recipeUpdate = String.format(
            "UPDATE %s.recipe SET recipe = ? WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement updateRecipePreparedStatement = connection.prepareStatement(recipeUpdate);
        updateRecipePreparedStatement.setString(1, updateOptions.recipe());
        updateRecipePreparedStatement.setLong(2, updateOptions.userId());
        updateRecipePreparedStatement.setString(3, updateOptions.dishName());
        return updateRecipePreparedStatement;
    }

    @Override
    public void updateDishName(@NonNull final DishUpdateOptions updateOptions) throws SQLException, Exception
    {
        executeAsTransaction(() -> {
            internalUpdateDishName(updateOptions);
        });
    }

    private void internalUpdateDishName(@NonNull final DishUpdateOptions updateOptions) throws SQLException
    {
        try (
            final PreparedStatement updateDishNamePreparedStatement = getUpdatePreparedDishNameStatement(updateOptions)
        ) {
            updateDishNamePreparedStatement.executeUpdate();
        }
    }

    @NonNull
    private PreparedStatement getUpdatePreparedDishNameStatement(@NonNull final DishUpdateOptions updateOptions) throws SQLException {
        final String dishNameUpdate = String.format(
            "UPDATE %s.recipe SET dish_name = ? WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement updateDishNamePreparedStatement = connection.prepareStatement(dishNameUpdate);
        updateDishNamePreparedStatement.setString(1, updateOptions.newDishName());
        updateDishNamePreparedStatement.setLong(2, updateOptions.userId());
        updateDishNamePreparedStatement.setString(3, updateOptions.dishName());
        return updateDishNamePreparedStatement;
    }

    @Override
    public void updateDishIngredientList(@NonNull final DishUpdateOptions updateOptions) throws SQLException, Exception
    {
        executeAsTransaction(() -> {
            internalUpdateDishIngredientList(updateOptions);
        });
    }

    private void internalUpdateDishIngredientList(@NonNull final DishUpdateOptions updateOptions) throws SQLException
    {
        final DishInsertOptions insertOptions = new DishInsertOptions(
            updateOptions.userId(),
            updateOptions.dishName(),
            updateOptions.ingredientList(),
            updateOptions.recipe()
        );
        try (
            final Statement dishStatement = connection.createStatement();
            final PreparedStatement deleteIngredientPreparedStatement = getDeletePreparedIngredientStatement(updateOptions);
            final PreparedStatement insertPreparedIngredientStatement = getInsertPreparedIngredientStatement(insertOptions);
        ) {
            dishStatement.addBatch(deleteIngredientPreparedStatement.toString());
            final List<String> ingredientList = insertOptions.ingredientList();
            if (ingredientList != null) {
                for (String ingredient : ingredientList) {
                    insertPreparedIngredientStatement.setString(3, ingredient);
                    dishStatement.addBatch(insertPreparedIngredientStatement.toString());
                }
            }
            dishStatement.executeBatch();
        }
    }

    @NonNull
    private PreparedStatement getDeletePreparedIngredientStatement(@NonNull final DishUpdateOptions updateOptions) throws SQLException
    {
        final String ingredientDelete = String.format(
            "DELETE FROM %s.ingredient WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement deleteIngredientPreparedStatement = connection.prepareStatement(ingredientDelete);
        deleteIngredientPreparedStatement.setLong(1, updateOptions.userId());
        deleteIngredientPreparedStatement.setString(2, updateOptions.dishName());
        return deleteIngredientPreparedStatement;
    }

    @Override
    public void updateDishRecipe(@NonNull final DishUpdateOptions updateOptions) throws SQLException
    {
        internalUpdateDishRecipe(updateOptions);
    }

    private void internalUpdateDishRecipe(@NonNull final DishUpdateOptions updateOptions) throws SQLException
    {
        try (
            final PreparedStatement updateRecipePreparedStatement = getUpdatePreparedRecipeStatement(updateOptions);
        ) {
            updateRecipePreparedStatement.executeUpdate();
        }
    }

    @Override
    @Nullable
    public UserContextDTO selectUserContext(@NonNull final UserContextSelectOptions selectOptions) throws SQLException
    {
        UserContextDTO userContextDTO;
        try (
            final PreparedStatement selectUserContextPreparedStatement = getSelectPreparedUserContextStatement(selectOptions);
            final ResultSet userContextResultSet = selectUserContextPreparedStatement.executeQuery();
        ) {
            userContextDTO = new UserContextDTO(userContextResultSet);
        } catch (NotFoundUserContextException e) {
            userContextDTO = null;
        }
        return userContextDTO;
    }

    @NonNull
    private PreparedStatement getSelectPreparedUserContextStatement(@NonNull final UserContextSelectOptions selectOptions) throws SQLException
    {
        final String userContextSelect = String.format(
            "SELECT multi_state_command_type, command_state, dish_name FROM %s.user_context WHERE user_id = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectUserContextPreparedStatement = connection.prepareStatement(userContextSelect);
        selectUserContextPreparedStatement.setLong(1, selectOptions.userId());
        return selectUserContextPreparedStatement;
    }

    @Override
    public void insertUserContext(@NonNull final UserContextInsertOptions insertOptions) throws SQLException {
        try (
            final PreparedStatement insertUserContextPreparedStatement = getInsertPreparedUserContextStatement(insertOptions);
        ) {
            insertUserContextPreparedStatement.executeUpdate();
        }
    }

    @NonNull
    private PreparedStatement getInsertPreparedUserContextStatement(@NonNull final UserContextInsertOptions insertOptions) throws SQLException
    {
        final String userContextInsert = String.format(
            "INSERT INTO %s.user_context (%s, %s, %s, %s) VALUES (?, ?::multi_state_command_types, ?::command_states, ?);",
            postgresDbDriverOptions.getDbSchema(),
            PostgresUserContextSchema.USER_ID,
            PostgresUserContextSchema.MULTI_STATE_COMMAND_TYPE,
            PostgresUserContextSchema.COMMAND_STATE,
            PostgresUserContextSchema.DISH_NAME
        );
        final PreparedStatement insertUserContextPreparedStatement = connection.prepareStatement(userContextInsert);
        insertUserContextPreparedStatement.setLong(1, insertOptions.userId());
        insertUserContextPreparedStatement.setString(2, insertOptions.multiStateCommandType().getValue());
        insertUserContextPreparedStatement.setString(3, insertOptions.commandState().getValue());
        insertUserContextPreparedStatement.setString(4, insertOptions.dishName());
        return insertUserContextPreparedStatement;
    }

    @Override
    public void deleteUserContext(@NonNull final UserContextDeleteOptions deleteOptions) throws SQLException
    {
        try (
            final PreparedStatement deleteUserContextPreparedStatement = getDeletePreparedUserContextStatement(deleteOptions);
        ) {
            deleteUserContextPreparedStatement.executeUpdate();
        }
    }

    @NonNull
    private PreparedStatement getDeletePreparedUserContextStatement(@NonNull final UserContextDeleteOptions deleteOptions) throws SQLException
    {
        final String userContextDelete = String.format(
            "DELETE FROM %s.user_context WHERE user_id = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement deleteUserContextPreparedStatement = connection.prepareStatement(userContextDelete);
        deleteUserContextPreparedStatement.setLong(1, deleteOptions.userId());
        return deleteUserContextPreparedStatement;
    }

    @Override
    public void updateUserContext(@NonNull final UserContextUpdateOptions updateOptions) throws SQLException
    {
        try (
            final PreparedStatement updateUserContextPreparedStatement = getUpdatePreparedUserContextStatement(updateOptions);
        ) {
            updateUserContextPreparedStatement.executeUpdate();
        }
    }

    @NonNull
    private PreparedStatement getUpdatePreparedUserContextStatement(@NonNull final UserContextUpdateOptions updateOptions) throws SQLException
    {
        final String userContextUpdate = String.format(
            "UPDATE %s.user_context SET command_state = ?::command_states, dish_name = ? WHERE user_id = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement updateUserContextPreparedStatement = connection.prepareStatement(userContextUpdate);
        updateUserContextPreparedStatement.setString(1, updateOptions.commandState().getValue());
        updateUserContextPreparedStatement.setString(2, updateOptions.dishName());
        updateUserContextPreparedStatement.setLong(3, updateOptions.userId());
        return updateUserContextPreparedStatement;
    }

    @Override
    public void updateUserContextCommandState(@NonNull final UserContextUpdateOptions updateOptions) throws SQLException
    {
        try (
            final PreparedStatement updateUserContextCommandStatePreparedStatement = getUpdatePreparedUserContextCommandStateStatement(updateOptions);
        ) {
            updateUserContextCommandStatePreparedStatement.executeUpdate();
        }
    }

    @NonNull
    private PreparedStatement getUpdatePreparedUserContextCommandStateStatement(@NonNull final UserContextUpdateOptions updateOptions) throws SQLException
    {
        final String userContextUpdate = String.format(
            "UPDATE %s.user_context SET command_state = ?::command_states WHERE user_id = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement updateUserContextPreparedStatement = connection.prepareStatement(userContextUpdate);
        updateUserContextPreparedStatement.setString(1, updateOptions.commandState().getValue());
        updateUserContextPreparedStatement.setLong(2, updateOptions.userId());
        return updateUserContextPreparedStatement;
    }

    @Override
    public void insertFeedback(@NonNull final FeedbackInsertOptions insertOptions) throws SQLException
    {
        try (
            final PreparedStatement insertFeedbackPreparedStatement = getInsertPreparedFeedbackStatement(insertOptions);
        ) {
            insertFeedbackPreparedStatement.execute();
        }
    }

    @NonNull
    private PreparedStatement getInsertPreparedFeedbackStatement(@NonNull final FeedbackInsertOptions insertOptions) throws SQLException
    {
        final String feedbackInsert = String.format(
            "INSERT INTO %s.feedback (%s, %s) VALUES (?, ?);",
            postgresDbDriverOptions.getDbSchema(),
            PostgresFeedbackSchema.USER_ID,
            PostgresFeedbackSchema.FEEDBACK
        );
        final PreparedStatement insertFeedbackPreparedStatement = connection.prepareStatement(feedbackInsert);
        insertFeedbackPreparedStatement.setLong(1, insertOptions.userId());
        insertFeedbackPreparedStatement.setString(2, insertOptions.feedback());
        return insertFeedbackPreparedStatement;
    }

    @Override
    public void close() throws SQLException
    {
        connection.close();
    }

    private void runInitScripts() throws SQLException, IOException
    {
        runScript(postgresDbDriverOptions.getInitSQLScriptPath());
    }

    private void runAlterScripts() throws SQLException, IOException
    {
        runScript(postgresDbDriverOptions.getAlterSQLScriptPath());
    }

    private void runScript(@NonNull final String filePath) throws SQLException, IOException
    {
        try (
            final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        ) {
            final ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.setLogWriter(null);
            scriptRunner.setSendFullScript(true);
            scriptRunner.runScript(bufferedReader);
        }
    }
}