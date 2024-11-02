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

import dbdrivers.DbDriver;
import models.db.drivers.SQLStatementBatch;
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

// TODO: Read about inheritance with annotations
public class PostgresDbDriver implements DbDriver {
    @NonNull
    private final PostgresDbDriverOptions postgresDbDriverOptions;

    @Nullable
    private Connection connection;

    public PostgresDbDriver(@NonNull final PostgresDbDriverOptions options) {
        postgresDbDriverOptions = options;
    }

    @Override
    public void connect() throws SQLException {
        Properties connectionProperties = new Properties();
        setConnectionProperties(connectionProperties);
        connection = DriverManager.getConnection(postgresDbDriverOptions.getDbUrl(), connectionProperties);
    }

    private void setConnectionProperties(@NonNull final Properties connectionProperties) {
        connectionProperties.setProperty(USER, postgresDbDriverOptions.getDbUsername());
        connectionProperties.setProperty(PASSWORD, postgresDbDriverOptions.getDbPassword());
        connectionProperties.setProperty(CURRENT_SCHEMA, postgresDbDriverOptions.getDbSchema());
    }

    @Override
    public void setup() throws SQLException, Exception {
        executeAsTransaction(() -> {
            runInitScripts();
            runAlterScripts();
        });
    }

    @Override
    public void executeAsTransaction(@NonNull SQLStatementBatch sqlStatementBatch) throws SQLException,
                                                                                          Exception
    {
        if (postgresDbDriverOptions.getIsDistributedDatabase()) {
            throw new Exception("Distributed database is not supported yet");
        }
        executeAsOnePhaseTransaction(sqlStatementBatch);
    }

    private void executeAsOnePhaseTransaction(@NonNull SQLStatementBatch sqlStatementBatch) throws SQLException,
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
    public DishDTO selectDish(@NonNull final DishSelectOptions selectOptions) throws SQLException {
        DishDTO dishDTO;
        try (
            final Statement selectDishStatement = connection.createStatement();
            final PreparedStatement selectPreparedRecipeStatement = getSelectPreparedRecipeStatement(selectOptions);
            final PreparedStatement selectPreparedDishIngredientListStatement = getSelectPreparedDishIngredientListStatement(selectOptions);
        ) {
            selectDishStatement.execute(
                String.format(
                    "%s; %s",
                    selectPreparedRecipeStatement.toString(),
                    selectPreparedDishIngredientListStatement.toString()
                )
            );      
            dishDTO = new DishDTO(selectDishStatement);
        } catch (NotFoundDishException e) {
            dishDTO = null;
        }
        return dishDTO;
    }

    @NonNull
    private PreparedStatement getSelectPreparedRecipeStatement(@NonNull final DishSelectOptions selectOptions) throws SQLException {
        final String recipeSelect = String.format(
            "SELECT dish_name, recipe FROM %s.recipe WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectPreparedRecipeStatement = connection.prepareStatement(recipeSelect);
        selectPreparedRecipeStatement.setLong(1, selectOptions.userId());
        selectPreparedRecipeStatement.setString(2, selectOptions.dishName());
        return selectPreparedRecipeStatement;
    }

    @NonNull
    private PreparedStatement getSelectPreparedDishIngredientListStatement(@NonNull final DishSelectOptions selectOptions) throws SQLException {
        final String dishIngredientListSelect = String.format(
            "SELECT ingredient FROM %s.ingredient WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectPreparedDishIngredientListStatement = connection.prepareStatement(dishIngredientListSelect);
        selectPreparedDishIngredientListStatement.setLong(1, selectOptions.userId());
        selectPreparedDishIngredientListStatement.setString(2, selectOptions.dishName());
        return selectPreparedDishIngredientListStatement;
    }

    @Override
    @Nullable
    public List<DishDTO> selectDishList(@NonNull final DishListSelectOptions selectOptions) throws SQLException {
        final ArrayList<DishDTO> dishList = new ArrayList<>();
        try (
            final PreparedStatement selectPreparedRecipeListStatement = getSelectPreparedRecipeListStatement(selectOptions);
            final PreparedStatement selectPreparedIngredientListStatement = getSelectPreparedIngredientListStatement(selectOptions);
            final ResultSet recipeListResultSet = selectPreparedRecipeListStatement.executeQuery();
        ) {
            String dishName, recipe;
            List<String> ingredientList;
            while (recipeListResultSet.next()) {
                dishName = recipeListResultSet.getString(PostgresRecipeSchema.DISH_NAME);
                recipe = recipeListResultSet.getString(PostgresRecipeSchema.RECIPE);
                selectPreparedIngredientListStatement.setString(2, dishName);
                ingredientList = getDishIngredientList(selectPreparedIngredientListStatement);
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
    private List<String> getDishIngredientList(@NonNull final PreparedStatement selectPreparedIngredientListStatement) throws SQLException {
        final List<String> ingredientList = new ArrayList<>();
        try (
            final ResultSet dishIngredientResultSet = selectPreparedIngredientListStatement.executeQuery();
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
    private PreparedStatement getSelectPreparedRecipeListStatement(@NonNull final DishListSelectOptions selectOptions) throws SQLException {
        final String recipeSelect = String.format(
            "SELECT dish_name, recipe FROM %s.recipe WHERE user_id = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectPreparedRecipeStatement = connection.prepareStatement(recipeSelect);
        selectPreparedRecipeStatement.setLong(1, selectOptions.userId());
        return selectPreparedRecipeStatement;
    }

    @NonNull
    private PreparedStatement getSelectPreparedIngredientListStatement(@NonNull final DishListSelectOptions selectOptions) throws SQLException {
        final String ingredientListSelect = String.format(
            "SELECT ingredient FROM %s.ingredient WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectPreparedIngredientListStatement = connection.prepareStatement(ingredientListSelect);
        selectPreparedIngredientListStatement.setLong(1, selectOptions.userId());
        return selectPreparedIngredientListStatement;
    }

    @Override
    public void insertDish(@NonNull final DishInsertOptions insertOptions) throws SQLException, Exception {
        executeAsTransaction(() -> {
            internalInsertDish(insertOptions);
        });
    }

    private void internalInsertDish(@NonNull final DishInsertOptions insertOptions) throws SQLException {
        try (
            final Statement dishStatement = connection.createStatement();
            final PreparedStatement insertPreparedRecipeStatement = getInsertPreparedRecipeStatement(insertOptions);
            final PreparedStatement insertPreparedIngredientStatement = getInsertPreparedIngredientStatement(insertOptions);
        ) {
            dishStatement.addBatch(insertPreparedRecipeStatement.toString());
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
    private PreparedStatement getInsertPreparedRecipeStatement(@NonNull final DishInsertOptions insertOptions) throws SQLException {
        final String recipeInsert = String.format(
            "INSERT INTO %s.recipe (%s, %s, %s) VALUES (?, ?, ?);",
            postgresDbDriverOptions.getDbSchema(),
            PostgresRecipeSchema.USER_ID,
            PostgresRecipeSchema.DISH_NAME,
            PostgresRecipeSchema.RECIPE
        );
        final PreparedStatement insertPreparedRecipeStatement = connection.prepareStatement(recipeInsert);
        insertPreparedRecipeStatement.setLong(1, insertOptions.userId());
        insertPreparedRecipeStatement.setString(2, insertOptions.dishName());
        insertPreparedRecipeStatement.setString(3, insertOptions.recipe());
        return insertPreparedRecipeStatement;
    }

    @NonNull
    private PreparedStatement getInsertPreparedIngredientStatement(@NonNull final DishInsertOptions insertOptions) throws SQLException {
        final String ingredientInsert = String.format(
            "INSERT INTO %s.ingredient (%s, %s, %s) VALUES (?, ?, ?);",
            postgresDbDriverOptions.getDbSchema(),
            PostgresIngredientSchema.USER_ID,
            PostgresIngredientSchema.DISH_NAME,
            PostgresIngredientSchema.INGREDIENT
        );
        final PreparedStatement insertPreparedIngredientStatement = connection.prepareStatement(ingredientInsert);
        insertPreparedIngredientStatement.setLong(1, insertOptions.userId());
        insertPreparedIngredientStatement.setString(2, insertOptions.dishName());
        return insertPreparedIngredientStatement;
    }

    @Override
    public void deleteDish(@NonNull final DishDeleteOptions deleteOptions) throws SQLException {
        deleteRecipe(deleteOptions);
    }

    private void deleteRecipe(@NonNull final DishDeleteOptions deleteOptions) throws SQLException {
        try (
            final PreparedStatement deletePreparedRecipeStatement = getDeletePreparedRecipeStatement(deleteOptions);
        ) {
            deletePreparedRecipeStatement.executeUpdate();
        }
    }

    @NonNull
    private PreparedStatement getDeletePreparedRecipeStatement(@NonNull final DishDeleteOptions deleteOptions) throws SQLException {
        final String recipeDelete = String.format(
            "DELETE FROM %s.recipe WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement deletePreparedRecipeStatement = connection.prepareStatement(recipeDelete);
        deletePreparedRecipeStatement.setLong(1, deleteOptions.userId());
        deletePreparedRecipeStatement.setString(2, deleteOptions.dishName());
        return deletePreparedRecipeStatement;
    }

    @Override
    public void updateDish(@NonNull final DishUpdateOptions updateOptions) throws SQLException, Exception {
        executeAsTransaction(() -> {
            internalUpdateDish(updateOptions);
        });
    }

    private void internalUpdateDish(@NonNull final DishUpdateOptions updateOptions) throws SQLException {
        final DishInsertOptions insertOptions = new DishInsertOptions(
            updateOptions.userId(),
            updateOptions.dishName(),
            updateOptions.ingredientList(),
            updateOptions.recipe()
        );
        try (
            final Statement dishStatement = connection.createStatement();
            final PreparedStatement updatePreparedRecipeStatement = getUpdatePreparedRecipeStatement(updateOptions);
            final PreparedStatement deletePreparedIngredientStatement = getDeletePreparedIngredientStatement(updateOptions);
            final PreparedStatement insertPreparedIngredientStatement = getInsertPreparedIngredientStatement(insertOptions);
        ) {
            dishStatement.addBatch(updatePreparedRecipeStatement.toString());
            dishStatement.addBatch(deletePreparedIngredientStatement.toString());
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
    private PreparedStatement getUpdatePreparedRecipeStatement(@NonNull final DishUpdateOptions updateOptions) throws SQLException {
        final String recipeUpdate = String.format(
            "UPDATE %s.recipe SET recipe = ? WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement updatePreparedRecipeStatement = connection.prepareStatement(recipeUpdate);
        updatePreparedRecipeStatement.setString(1, updateOptions.recipe());
        updatePreparedRecipeStatement.setLong(2, updateOptions.userId());
        updatePreparedRecipeStatement.setString(3, updateOptions.dishName());
        return updatePreparedRecipeStatement;
    }

    @Override
    public void updateDishIngredientList(@NonNull final DishUpdateOptions updateOptions) throws SQLException, Exception {
        executeAsTransaction(() -> {
            internalUpdateDishIngredientList(updateOptions);
        });
    }

    private void internalUpdateDishIngredientList(@NonNull final DishUpdateOptions updateOptions) throws SQLException {
        final DishInsertOptions insertOptions = new DishInsertOptions(
            updateOptions.userId(),
            updateOptions.dishName(),
            updateOptions.ingredientList(),
            updateOptions.recipe()
        );
        try (
            final Statement dishStatement = connection.createStatement();
            final PreparedStatement deletePreparedIngredientStatement = getDeletePreparedIngredientStatement(updateOptions);
            final PreparedStatement insertPreparedIngredientStatement = getInsertPreparedIngredientStatement(insertOptions);
        ) {
            dishStatement.addBatch(deletePreparedIngredientStatement.toString());
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
    private PreparedStatement getDeletePreparedIngredientStatement(@NonNull final DishUpdateOptions updateOptions) throws SQLException {
        final String ingredientDelete = String.format(
            "DELETE FROM %s.ingredient WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement deletePreparedIngredientStatement = connection.prepareStatement(ingredientDelete);
        deletePreparedIngredientStatement.setLong(1, updateOptions.userId());
        deletePreparedIngredientStatement.setString(2, updateOptions.dishName());
        return deletePreparedIngredientStatement;
    }

    @Override
    public void updateDishRecipe(@NonNull final DishUpdateOptions updateOptions) throws SQLException {
        internalUpdateDishRecipe(updateOptions);
    }

    private void internalUpdateDishRecipe(@NonNull final DishUpdateOptions updateOptions) throws SQLException {
        try (
            final PreparedStatement updatePreparedRecipeStatement = getUpdatePreparedRecipeStatement(updateOptions);
        ) {
            updatePreparedRecipeStatement.executeUpdate();
        }
    }

    @Override
    @Nullable
    public UserContextDTO selectUserContext(@NonNull final UserContextSelectOptions selectOptions) throws SQLException {
        UserContextDTO userContextDTO;
        try (
            final PreparedStatement selectPreparedUserContextStatement = getSelectPreparedUserContextStatement(selectOptions);
            final ResultSet userContextResultSet = selectPreparedUserContextStatement.executeQuery();
        ) {
            userContextDTO = new UserContextDTO(userContextResultSet);
        } catch (NotFoundUserContextException e) {
            userContextDTO = null;
        }
        return userContextDTO;
    }

    @NonNull
    private PreparedStatement getSelectPreparedUserContextStatement(@NonNull final UserContextSelectOptions selectOptions) throws SQLException {
        final String userContextSelect = String.format(
            "SELECT multi_state_command_type, command_state, dish_name FROM %s.user_context WHERE user_id = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectPreparedUserContextStatement = connection.prepareStatement(userContextSelect);
        selectPreparedUserContextStatement.setLong(1, selectOptions.userId());
        return selectPreparedUserContextStatement;
    }

    @Override
    public void insertUserContext(@NonNull final UserContextInsertOptions insertOptions) throws SQLException {
        try (
            final PreparedStatement insertPreparedUserContextStatement = getInsertPreparedUserContextStatement(insertOptions);
        ) {
            insertPreparedUserContextStatement.executeUpdate();
        }
    }

    @NonNull
    private PreparedStatement getInsertPreparedUserContextStatement(@NonNull final UserContextInsertOptions insertOptions) throws SQLException {
        final String userContextInsert = String.format(
            "INSERT INTO %s.user_context (%s, %s, %s, %s) VALUES (?, ?::multi_state_command_types, ?::command_states, ?);",
            postgresDbDriverOptions.getDbSchema(),
            PostgresUserContextSchema.USER_ID,
            PostgresUserContextSchema.MULTI_STATE_COMMAND_TYPE,
            PostgresUserContextSchema.COMMAND_STATE,
            PostgresUserContextSchema.DISH_NAME
        );
        final PreparedStatement insertPreparedUserContextStatement = connection.prepareStatement(userContextInsert);
        insertPreparedUserContextStatement.setLong(1, insertOptions.userId());
        insertPreparedUserContextStatement.setString(2, insertOptions.multiStateCommantType().getValue());
        insertPreparedUserContextStatement.setString(3, insertOptions.commandState().getValue());
        insertPreparedUserContextStatement.setString(4, insertOptions.dishName());
        return insertPreparedUserContextStatement;
    }

    @Override
    public void deleteUserContext(@NonNull final UserContextDeleteOptions deleteOptions) throws SQLException {
        try (
            final PreparedStatement deletePreparedUserContextStatement = getDeletePreparedUserContextStatement(deleteOptions);
        ) {
            deletePreparedUserContextStatement.executeUpdate();
        }
    }

    @NonNull
    private PreparedStatement getDeletePreparedUserContextStatement(@NonNull final UserContextDeleteOptions deleteOptions) throws SQLException {
        final String userContextDelete = String.format(
            "DELETE FROM %s.user_context WHERE user_id = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement deletePreparedUserContextStatement = connection.prepareStatement(userContextDelete);
        deletePreparedUserContextStatement.setLong(1, deleteOptions.userId());
        return deletePreparedUserContextStatement;
    }

    @Override
    public void updateUserContext(@NonNull final UserContextUpdateOptions updateOptions) throws SQLException {
        try (
            final PreparedStatement updatePreparedUserContextStatement = getUpdatePreparedUserContextStatement(updateOptions);
        ) {
            updatePreparedUserContextStatement.executeUpdate();
        }
    }

    @NonNull
    private PreparedStatement getUpdatePreparedUserContextStatement(@NonNull final UserContextUpdateOptions updateOptions) throws SQLException {
        final String userContextUpdate = String.format(
            "UPDATE %s.user_context SET command_state = ?::command_states, dish_name = ? WHERE user_id = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement updatePreparedUserContextStatement = connection.prepareStatement(userContextUpdate);
        updatePreparedUserContextStatement.setString(1, updateOptions.commandState().getValue());
        updatePreparedUserContextStatement.setString(2, updateOptions.dishName());
        updatePreparedUserContextStatement.setLong(3, updateOptions.userId());
        return updatePreparedUserContextStatement;
    }

    @Override
    public void updateUserContextCommandState(@NonNull final UserContextUpdateOptions updateOptions) throws SQLException {
        try (
            final PreparedStatement updatePreparedUserContextCommandStateStatement = getUpdatePreparedUserContextCommandStateStatement(updateOptions);
        ) {
            updatePreparedUserContextCommandStateStatement.executeUpdate();
        }
    }

    @NonNull
    private PreparedStatement getUpdatePreparedUserContextCommandStateStatement(@NonNull final UserContextUpdateOptions updateOptions) throws SQLException {
        final String userContextUpdate = String.format(
            "UPDATE %s.user_context SET command_state = ?::command_states WHERE user_id = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement updatePreparedUserContextStatement = connection.prepareStatement(userContextUpdate);
        updatePreparedUserContextStatement.setString(1, updateOptions.commandState().getValue());
        updatePreparedUserContextStatement.setLong(2, updateOptions.userId());
        return updatePreparedUserContextStatement;
    }

    @Override
    public void insertFeedback(@NonNull final FeedbackInsertOptions insertOptions) throws SQLException {
        try (
            final PreparedStatement insertPreparedFeedbackStatement = getInsertPreparedFeedbackStatement(insertOptions);
        ) {
            insertPreparedFeedbackStatement.execute();
        }
    }

    @NonNull
    private PreparedStatement getInsertPreparedFeedbackStatement(@NonNull final FeedbackInsertOptions insertOptions) throws SQLException {
        final String feedbackInsert = String.format(
            "INSERT INTO %s.feedback (%s, %s) VALUES (?, ?);",
            postgresDbDriverOptions.getDbSchema(),
            PostgresFeedbackSchema.USER_ID,
            PostgresFeedbackSchema.FEEDBACK
        );
        final PreparedStatement insertPreparedFeedbackStatement = connection.prepareStatement(feedbackInsert);
        insertPreparedFeedbackStatement.setLong(1, insertOptions.userId());
        insertPreparedFeedbackStatement.setString(2, insertOptions.feedback());
        return insertPreparedFeedbackStatement;
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    private void runInitScripts() throws SQLException, IOException {
        runScript(postgresDbDriverOptions.getInitSQLScriptPath());
    }

    private void runAlterScripts() throws SQLException, IOException {
        runScript(postgresDbDriverOptions.getAlterSQLScriptPath());
    }

    private void runScript(@NonNull final String filePath) throws SQLException, IOException {
        try (
            final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        ) {
            final ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.setSendFullScript(true);
            scriptRunner.runScript(bufferedReader);
        }
    }
}