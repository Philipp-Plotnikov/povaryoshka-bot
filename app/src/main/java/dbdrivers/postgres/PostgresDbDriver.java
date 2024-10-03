package dbdrivers.postgres;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.jdbc.ScriptRunner;

import dbdrivers.DbDriver;
import models.dbdrivers.SQLStatementBatch;
import static models.dbdrivers.postgres.PostgresConnectionProperties.CURRENT_SCHEMA;
import static models.dbdrivers.postgres.PostgresConnectionProperties.PASSWORD;
import static models.dbdrivers.postgres.PostgresConnectionProperties.USER;
import models.dbdrivers.postgres.PostgresDbDriverOptions;
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

// TODO: Not convenince with ? as I need to remember the order, it needs to make easier
public class PostgresDbDriver implements DbDriver {
    private static PostgresDbDriver instance;
    private final PostgresDbDriverOptions postgresDbDriverOptions;

    private Connection connection;

    private PostgresDbDriver(final PostgresDbDriverOptions options) {
        postgresDbDriverOptions = options;
    }

    public static PostgresDbDriver getInstance(final PostgresDbDriverOptions options) {
        if (instance == null) {
            instance = new PostgresDbDriver(options);
        }
        return instance;
    }

    @Override
    public void connect() throws SQLException {
        Properties connectionProperties = new Properties();
        setConnectionProperties(connectionProperties);
        connection = DriverManager.getConnection(postgresDbDriverOptions.getDbUrl(), connectionProperties);
    }

    private void setConnectionProperties(final Properties connectionProperties) {
        connectionProperties.setProperty(USER, postgresDbDriverOptions.getDbUsername());
        connectionProperties.setProperty(PASSWORD, postgresDbDriverOptions.getDbPassword());
        connectionProperties.setProperty(CURRENT_SCHEMA, postgresDbDriverOptions.getDbSchema());
    }

    @Override
    public void setup() throws SQLException, Exception {
        runAsTransaction(() -> {
            runInitScripts();
            runAlterScripts();
        });
    }

    // TODO: Check it works
    // TODO: Think how to handle distributes transaction
    @Override
    public void runAsTransaction(SQLStatementBatch sqlStatementBatch) throws SQLException, Exception {
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

    // TODO: we can make one request and get results but do we need it ?
    // Check we have methods for large query what is it
    @Override
    public DishDTO selectDish(final DishSelectOptions selectOptions) throws SQLException {
        DishDTO dishDTO;
        try (
            final PreparedStatement selectPreparedRecipeStatement = getSelectPreparedRecipeStatement(selectOptions);
            final PreparedStatement selectPreparedIngredientStatement = getSelectPreparedIngredientStatement(selectOptions);
            final ResultSet recipeResultSet = selectPreparedRecipeStatement.executeQuery();
            final ResultSet ingredientResultSet = selectPreparedIngredientStatement.executeQuery();
        ) {
            dishDTO = new DishDTO(recipeResultSet, ingredientResultSet);
        }
        return dishDTO;
    }

    private PreparedStatement getSelectPreparedRecipeStatement(final DishSelectOptions selectOptions) throws SQLException {
        final String recipeSelect = String.format(
            "SELECT dish_name, recipe FROM %s.recipe WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectPreparedRecipeStatement = connection.prepareStatement(recipeSelect);
        selectPreparedRecipeStatement.setLong(1, selectOptions.userId());
        selectPreparedRecipeStatement.setString(2, selectOptions.dishName());
        return selectPreparedRecipeStatement;
    }

    private PreparedStatement getSelectPreparedIngredientStatement(final DishSelectOptions selectOptions) throws SQLException {
        final String ingredientSelect = String.format(
            "SELECT ingredient FROM %s.ingredient WHERE user_id = ? AND dish_name = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectPreparedIngredientStatement = connection.prepareStatement(ingredientSelect);
        selectPreparedIngredientStatement.setLong(1, selectOptions.userId());
        selectPreparedIngredientStatement.setString(2, selectOptions.dishName());
        return selectPreparedIngredientStatement;
    }

    @Override
    public void insertDish(final DishInsertOptions insertOptions) throws SQLException, Exception {
        runAsTransaction(() -> {
            internalInsertDish(insertOptions);
        });
    }

    private void internalInsertDish(final DishInsertOptions insertOptions) throws SQLException {
        try (
            final Statement dishStatement = connection.createStatement();
            final PreparedStatement insertPreparedRecipeStatement = getInsertPreparedRecipeStatement(insertOptions);
            final PreparedStatement insertPreparedIngredientStatement = getInsertPreparedIngredientStatement(insertOptions);
        ) {
            dishStatement.addBatch(insertPreparedRecipeStatement.toString());
            final List<String> ingredientList = insertOptions.ingredientList();
            for (String ingredient : ingredientList) {
                insertPreparedIngredientStatement.setString(3, ingredient);
                dishStatement.addBatch(insertPreparedIngredientStatement.toString());
            }
            dishStatement.executeBatch();
        }
    }

    private PreparedStatement getInsertPreparedRecipeStatement(final DishInsertOptions insertOptions) throws SQLException {
        final String recipeInsert = String.format(
            "INSERT INTO %s.recipe VALUES (?, ?, ?);",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement insertPreparedRecipeStatement = connection.prepareStatement(recipeInsert);
        insertPreparedRecipeStatement.setLong(1, insertOptions.userId());
        insertPreparedRecipeStatement.setString(2, insertOptions.dishName());
        insertPreparedRecipeStatement.setString(3, insertOptions.recipe());
        return insertPreparedRecipeStatement;
    }

    private PreparedStatement getInsertPreparedIngredientStatement(final DishInsertOptions insertOptions) throws SQLException {
        final String ingredientInsert = String.format(
            "INSERT INTO %s.ingredient VALUES (?, ?, ?);",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement insertPreparedIngredientStatement = connection.prepareStatement(ingredientInsert);
        insertPreparedIngredientStatement.setLong(1, insertOptions.userId());
        insertPreparedIngredientStatement.setString(2, insertOptions.dishName());
        return insertPreparedIngredientStatement;
    }

    @Override
    public void deleteDish(final DishDeleteOptions deleteOptions) throws SQLException {
        deleteRecipe(deleteOptions);
    }

    private void deleteRecipe(final DishDeleteOptions deleteOptions) throws SQLException {
        try (
            final PreparedStatement deletePreparedRecipeStatement = getDeletePreparedRecipeStatement(deleteOptions);
        ) {
            deletePreparedRecipeStatement.executeUpdate();
        }
    }

    private PreparedStatement getDeletePreparedRecipeStatement(final DishDeleteOptions deleteOptions) throws SQLException {
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
    public void updateDish(final DishUpdateOptions updateOptions) throws SQLException, Exception {
        runAsTransaction(() -> {
            internalUpdateDish(updateOptions);
        });
    }

    private void internalUpdateDish(final DishUpdateOptions updateOptions) throws SQLException {
        final DishInsertOptions insertOptions = new DishInsertOptions(
            updateOptions.userId(),
            updateOptions.dishName(),
            updateOptions.ingredientList(),
            updateOptions.recipe()
        );
        try (
            final PreparedStatement updatePreparedRecipeStatement = getUpdatePreparedRecipeStatement(updateOptions);
            final PreparedStatement deletePreparedIngredientStatement = getDeletePreparedIngredientStatement(updateOptions);
            final PreparedStatement insertPreparedIngredientStatement = getInsertPreparedIngredientStatement(insertOptions);
            final Statement dishStatement = connection.createStatement();
        ) {
            dishStatement.addBatch(updatePreparedRecipeStatement.toString());
            dishStatement.addBatch(deletePreparedIngredientStatement.toString());
            final List<String> ingredientList = insertOptions.ingredientList();
            for (String ingredient : ingredientList) {
                insertPreparedIngredientStatement.setString(3, ingredient);
                dishStatement.addBatch(insertPreparedIngredientStatement.toString());
            }
            dishStatement.executeBatch();
        }
    }

    private PreparedStatement getUpdatePreparedRecipeStatement(final DishUpdateOptions updateOptions) throws SQLException {
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

    private PreparedStatement getDeletePreparedIngredientStatement(final DishUpdateOptions updateOptions) throws SQLException {
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
    public UserContextDTO selectUserContext(final UserContextSelectOptions selectOptions) throws SQLException {
        UserContextDTO userContextDTO;
        try (
            final PreparedStatement selectPreparedUserContextStatement = getSelectPreparedUserContextStatement(selectOptions);
            final ResultSet userContextResultSet = selectPreparedUserContextStatement.executeQuery();
        ) {
            userContextDTO = new UserContextDTO(userContextResultSet);
        }
        return userContextDTO;
    }

    private PreparedStatement getSelectPreparedUserContextStatement(final UserContextSelectOptions selectOptions) throws SQLException {
        final String userContextSelect = String.format(
            "SELECT multi_state_command_type, command_state FROM %s.user_context WHERE user_id = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectPreparedUserContextStatement = connection.prepareStatement(userContextSelect);
        selectPreparedUserContextStatement.setLong(1, selectOptions.userId());
        return selectPreparedUserContextStatement;
    }

    @Override
    public void insertUserContext(final UserContextInsertOptions insertOptions) throws SQLException {
        try (
            final PreparedStatement insertPreparedUserContextStatement = getInsertPreparedUserContextStatement(insertOptions);
        ) {
            insertPreparedUserContextStatement.executeUpdate();
        }
    }

    // TODO: What about SQLData interface to insert enum
    // Does this approach have benefits ?
    private PreparedStatement getInsertPreparedUserContextStatement(final UserContextInsertOptions insertOptions) throws SQLException {
        final String userContextInsert = String.format(
            "INSERT INTO %s.user_context VALUES (?, ?, ?);",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement insertPreparedUserContextStatement = connection.prepareStatement(userContextInsert);
        insertPreparedUserContextStatement.setLong(1, insertOptions.userId());
        insertPreparedUserContextStatement.setString(2, insertOptions.multiStateCommantType().getValue());
        insertPreparedUserContextStatement.setString(3, insertOptions.commandState().getValue());
        return insertPreparedUserContextStatement;
    }

    @Override
    public void deleteUserContext(final UserContextDeleteOptions deleteOptions) throws SQLException {
        try (
            final PreparedStatement deletePreparedUserContextStatement = getDeletePreparedUserContextStatement(deleteOptions);
        ) {
            deletePreparedUserContextStatement.executeUpdate();
        }
    }

    private PreparedStatement getDeletePreparedUserContextStatement(final UserContextDeleteOptions deleteOptions) throws SQLException {
        final String userContextDelete = String.format(
            "DELETE FROM %s.user_context WHERE user_id = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement deletePreparedUserContextStatement = connection.prepareStatement(userContextDelete);
        deletePreparedUserContextStatement.setLong(1, deleteOptions.userId());
        return deletePreparedUserContextStatement;
    }

    @Override
    public void updateUserContext(final UserContextUpdateOptions updateOptions) throws SQLException {
        try (
            final PreparedStatement updatePreparedUserContextStatement = getUpdatePreparedUserContextStatement(updateOptions);
        ) {
            updatePreparedUserContextStatement.executeUpdate();
        }
    }

    // TODO: What about SQLData interface to insert enum
    // Does this approach have benefits ?
    private PreparedStatement getUpdatePreparedUserContextStatement(final UserContextUpdateOptions updateOptions) throws SQLException {
        final String userContextUpdate = String.format(
            "UPDATE %s.user_context SET command_state = ? WHERE user_id = ?;",
            postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement updatePreparedUserContextStatement = connection.prepareStatement(userContextUpdate);
        updatePreparedUserContextStatement.setString(1, updateOptions.commandState().getValue());
        updatePreparedUserContextStatement.setLong(2, updateOptions.userId());
        return updatePreparedUserContextStatement;
    }

    @Override
    public void insertFeedback(final FeedbackInsertOptions insertOptions) throws SQLException {
        try (
            final PreparedStatement insertPreparedFeedbackStatement = getInsertPreparedFeedbackStatement(insertOptions);
        ) {
            insertPreparedFeedbackStatement.execute();
        }
    }

    private PreparedStatement getInsertPreparedFeedbackStatement(final FeedbackInsertOptions insertOptions) throws SQLException {
        final String feedbackInsert = String.format(
            "INSERT INTO %s.feedback (user_id, feedback) VALUES (?, ?);",
            postgresDbDriverOptions.getDbSchema()
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

    private void runInitScripts() throws SQLException, Exception {
        runScript(postgresDbDriverOptions.getInitSQLScriptPath());
    }

    private void runAlterScripts() throws SQLException, Exception {
        runScript(postgresDbDriverOptions.getAlterSQLScriptPath());
    }

    private void runScript(final String filePath) throws SQLException, Exception {
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