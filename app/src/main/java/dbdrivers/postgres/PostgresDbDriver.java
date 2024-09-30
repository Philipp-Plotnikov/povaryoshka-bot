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

import dbdrivers.DbDriver;
import static models.dbdrivers.postgres.PostgresConnectionProperties.CURRENT_SCHEMA;
import static models.dbdrivers.postgres.PostgresConnectionProperties.PASSWORD;
import static models.dbdrivers.postgres.PostgresConnectionProperties.USER;
import models.dbdrivers.postgres.PostgresDbDriverOptions;
import models.dbdrivers.postgres.PostgresSQLStatementBatch;
import models.dtos.DishDTO;
import models.sqlops.dish.DishDeleteOptions;
import models.sqlops.dish.DishInsertOptions;
import models.sqlops.dish.DishSelectOptions;
import models.sqlops.dish.DishUpdateOptions;
import models.sqlops.feedback.FeedbackInsertOptions;

public class PostgresDbDriver implements DbDriver {
    private final PostgresDbDriverOptions postgresDbDriverOptions;

    private Connection connection;

    public PostgresDbDriver(final PostgresDbDriverOptions options) {
        this.postgresDbDriverOptions = options;
    }

    @Override
    public void connect() throws SQLException {
        Properties connectionProperties = new Properties();
        this.setConnectionProperties(connectionProperties);
        this.connection = DriverManager.getConnection(this.postgresDbDriverOptions.getDbUrl(), connectionProperties);
    }

    private void setConnectionProperties(final Properties connectionProperties) {
        connectionProperties.setProperty(USER, this.postgresDbDriverOptions.getDbUsername());
        connectionProperties.setProperty(PASSWORD, this.postgresDbDriverOptions.getDbPassword());
        connectionProperties.setProperty(CURRENT_SCHEMA, this.postgresDbDriverOptions.getDbSchema());
    }

    @Override
    public void setup() throws Exception {
        this.runAsTransaction(() -> {
            this.runInitScripts();
            this.runAlterScripts();
        });
    }

    @Override
    public DishDTO selectDish(final DishSelectOptions selectOptions) throws SQLException {
        DishDTO dishDTO;
        try (
            final PreparedStatement selectPreparedRecipeStatement = this.getSelectPreparedRecipeStatement(selectOptions);
            final PreparedStatement selectPreparedIngredientStatement = this.getSelectPreparedIngredientStatement(selectOptions);
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
            this.postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectPreparedRecipeStatement = this.connection.prepareStatement(recipeSelect);
        selectPreparedRecipeStatement.setLong(1, selectOptions.getUserId());
        selectPreparedRecipeStatement.setString(2, selectOptions.getDishName());
        return selectPreparedRecipeStatement;
    }

    private PreparedStatement getSelectPreparedIngredientStatement(final DishSelectOptions selectOptions) throws SQLException {
        final String ingredientSelect = String.format(
            "SELECT ingredient FROM %s.ingredient WHERE user_id = ? AND dish_name = ?;",
            this.postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement selectPreparedIngredientStatement = this.connection.prepareStatement(ingredientSelect);
        selectPreparedIngredientStatement.setLong(1, selectOptions.getUserId());
        selectPreparedIngredientStatement.setString(2, selectOptions.getDishName());
        return selectPreparedIngredientStatement;
    }

    @Override
    public void insertDish(final DishInsertOptions insertOptions) throws SQLException, Exception {
        this.runAsTransaction(() -> {
            this.internalInsertDish(insertOptions);
        });
    }

    private void internalInsertDish(final DishInsertOptions insertOptions) throws SQLException {
        try (
            final Statement dishStatement = this.connection.createStatement();
            final PreparedStatement insertPreparedRecipeStatement = this.getInsertPreparedRecipeStatement(insertOptions);
            final PreparedStatement insertPreparedIngredientStatement = this.getInsertPreparedIngredientStatement(insertOptions);
        ) {
            dishStatement.addBatch(insertPreparedRecipeStatement.toString());
            final List<String> ingredientList = insertOptions.getIngredientList();
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
            this.postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement insertPreparedRecipeStatement = this.connection.prepareStatement(recipeInsert);
        insertPreparedRecipeStatement.setLong(1, insertOptions.getUserId());
        insertPreparedRecipeStatement.setString(2, insertOptions.getDishName());
        insertPreparedRecipeStatement.setString(3, insertOptions.getRecipe());
        return insertPreparedRecipeStatement;
    }

    private PreparedStatement getInsertPreparedIngredientStatement(final DishInsertOptions insertOptions) throws SQLException {
        final String ingredientInsert = String.format(
            "INSERT INTO %s.ingredient VALUES (?, ?, ?);",
            this.postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement insertPreparedIngredientStatement = this.connection.prepareStatement(ingredientInsert);
        insertPreparedIngredientStatement.setLong(1, insertOptions.getUserId());
        insertPreparedIngredientStatement.setString(2, insertOptions.getDishName());
        return insertPreparedIngredientStatement;
    }

    @Override
    public void deleteDish(final DishDeleteOptions deleteOptions) throws SQLException {
        this.deleteRecipe(deleteOptions);
    }

    private void deleteRecipe(final DishDeleteOptions deleteOptions) throws SQLException {
        try (
            final PreparedStatement deletePreparedRecipeStatement = this.getDeletePreparedRecipeStatement(deleteOptions);
        ) {
            deletePreparedRecipeStatement.executeUpdate();
        }
    }

    private PreparedStatement getDeletePreparedRecipeStatement(final DishDeleteOptions deleteOptions) throws SQLException {
        final String recipeDelete = String.format(
            "DELETE FROM %s.recipe WHERE user_id = ? AND dish_name = ?;",
            this.postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement deletePreparedRecipeStatement = this.connection.prepareStatement(recipeDelete);
        deletePreparedRecipeStatement.setLong(1, deleteOptions.getUserId());
        deletePreparedRecipeStatement.setString(2, deleteOptions.getDishName());
        return deletePreparedRecipeStatement;
    }

    @Override
    public void updateDish(final DishUpdateOptions updateOptions) throws SQLException, Exception {
        this.runAsTransaction(() -> {
            this.internalUpdateDish(updateOptions);
        });
    }

    private void internalUpdateDish(final DishUpdateOptions updateOptions) throws SQLException {
        final DishInsertOptions insertOptions = new DishInsertOptions(
            updateOptions.getUserId(),
            updateOptions.getDishName(),
            updateOptions.getIngredientList(),
            updateOptions.getRecipe()
        );
        try (
            final Statement dishStatement = this.connection.createStatement();
            final PreparedStatement updatePreparedRecipeStatement = this.getUpdatePreparedRecipeStatement(updateOptions);
            final PreparedStatement deletePreparedIngredientStatement = this.getDeletePreparedIngredientStatement(updateOptions);
            final PreparedStatement insertPreparedIngredientStatement = this.getInsertPreparedIngredientStatement(insertOptions);
        ) {
            dishStatement.addBatch(updatePreparedRecipeStatement.toString());
            dishStatement.addBatch(deletePreparedIngredientStatement.toString());
            final List<String> ingredientList = insertOptions.getIngredientList();
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
            this.postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement updatePreparedRecipeStatement = this.connection.prepareStatement(recipeUpdate);
        updatePreparedRecipeStatement.setString(1, updateOptions.getRecipe());
        updatePreparedRecipeStatement.setLong(2, updateOptions.getUserId());
        updatePreparedRecipeStatement.setString(3, updateOptions.getDishName());
        return updatePreparedRecipeStatement;
    }

    private PreparedStatement getDeletePreparedIngredientStatement(final DishUpdateOptions updateOptions) throws SQLException {
        final String ingredientDelete = String.format(
            "DELETE FROM %s.ingredient WHERE user_id = ? AND dish_name = ?;",
            this.postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement deletePreparedIngredientStatement = this.connection.prepareStatement(ingredientDelete);
        deletePreparedIngredientStatement.setLong(1, updateOptions.getUserId());
        deletePreparedIngredientStatement.setString(2, updateOptions.getDishName());
        return deletePreparedIngredientStatement;
    }

    @Override
    public void insertFeedback(final FeedbackInsertOptions insertOptions) throws SQLException {
        try (
            final PreparedStatement insertPreparedFeedbackStatement = this.getInsertPreparedFeedbackStatement(insertOptions);
        ) {
            insertPreparedFeedbackStatement.execute();
        }
    }

    private PreparedStatement getInsertPreparedFeedbackStatement(final FeedbackInsertOptions insertOptions) throws SQLException {
        final String feedbackInsert = String.format(
            "INSERT INTO %s.feedback (user_id, feedback) VALUES (?, ?);",
            this.postgresDbDriverOptions.getDbSchema()
        );
        final PreparedStatement insertPreparedFeedbackStatement = this.connection.prepareStatement(feedbackInsert);
        insertPreparedFeedbackStatement.setLong(1, insertOptions.getUserId());
        insertPreparedFeedbackStatement.setString(2, insertOptions.getFeedback());
        return insertPreparedFeedbackStatement;
    }

    @Override
    public void close() throws SQLException {
        this.connection.close();
    }

    private void runInitScripts() throws Exception {
        this.runScript("sql/postgres/init.sql");
    }

    private void runAlterScripts() throws Exception {
        this.runScript("sql/postgres/alter.sql");
    }

    private void runScript(final String filePath) throws SQLException, Exception {
        try (
            final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath);
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            final Statement statement = connection.createStatement();
        ) {
            final StringBuilder query = new StringBuilder();
            final String COMMENT_START_SIGN = "-- ";
            final String SPACE = " ";
            final String TERMINATED_STATEMENT_SIGN = ";";
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.trim().startsWith(COMMENT_START_SIGN)) {
                    continue;
                }
                query.append(line).append(SPACE);
                if (line.trim().endsWith(TERMINATED_STATEMENT_SIGN)) {
                    statement.execute(query.toString().trim());
                    query.setLength(0);
                }
            }
        }
    }

    private void runAsTransaction(PostgresSQLStatementBatch sqlStatementBatch) throws SQLException, Exception {
        try {
            this.connection.setAutoCommit(false);
            sqlStatementBatch.execute();
            this.connection.commit();
        } catch(Exception e) {
            this.connection.rollback();
            throw e;
        } finally {
            this.connection.setAutoCommit(true);
        }
    }
}