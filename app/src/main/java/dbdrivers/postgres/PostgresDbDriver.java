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
import java.util.ArrayList;
import java.util.Properties;

import dbdrivers.AbstractDbDriver;
import models.Dish;
import models.dbdrivers.postgres.PostgresDbDriverOptions;
import models.sqlops.DeleteOptions;
import models.sqlops.InsertOptions;
import models.sqlops.SelectOptions;
import models.sqlops.UpdateOptions;

public class PostgresDbDriver extends AbstractDbDriver {
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
        connectionProperties.setProperty("user", this.postgresDbDriverOptions.getDbUsername());
        connectionProperties.setProperty("password", this.postgresDbDriverOptions.getDbPassword());
        connectionProperties.setProperty("currentSchema", this.postgresDbDriverOptions.getDbSchema());
    }

    @Override
    public void setup() throws Exception {
        this.runInitScripts();
        this.runAlterScripts();
    }

    /*
        TODO: Think how to make optimal query
    */
    @Override
    public Dish selectDish(final SelectOptions selectOptions) throws SQLException {
        Dish dish;
        final String recipeSelect = String.format(
            "SELECT dish_name, recipe FROM %s.recipe WHERE user_id = %d AND dish_name = '%s';",
            this.postgresDbDriverOptions.getDbSchema(),
            selectOptions.getUserId(),
            selectOptions.getDishName()
        );
        final String ingredientSelect = String.format(
            "SELECT ingredient FROM %s.ingredient WHERE user_id = %d AND dish_name = '%s';",
            this.postgresDbDriverOptions.getDbSchema(),
            selectOptions.getUserId(),
            selectOptions.getDishName()
        );
        try (
            final Statement recipeStatement = this.connection.createStatement();
            final Statement ingredientStatement = this.connection.createStatement();
            final ResultSet recipeResultSet = recipeStatement.executeQuery(recipeSelect);
            final ResultSet ingredientResultSet = ingredientStatement.executeQuery(ingredientSelect);
        ) {
            dish = new Dish(
                recipeResultSet,
                ingredientResultSet
            );
        }
        return dish;
    }

    @Override
    public void insertDish(final InsertOptions insertOptions) throws SQLException {
        this.insertRecipe(insertOptions);
        this.insertIngredients(insertOptions);
    }

    private void insertRecipe(final InsertOptions insertOptions) throws SQLException {
        final String recipeInsert = String.format(
            "INSERT INTO %s.recipe VALUES (%d, '%s', '%s');",
            this.postgresDbDriverOptions.getDbSchema(),
            insertOptions.getUserId(),
            insertOptions.getDishName(),
            insertOptions.getRecipe()
        );
        try (
            final Statement recipeStatement = this.connection.createStatement();
        ) {
            recipeStatement.executeUpdate(recipeInsert);
        }
    }

    private void insertIngredients(final InsertOptions insertOptions) throws SQLException {
        final String ingredientInsert = String.format(
            "INSERT INTO %s.ingredient VALUES (%d, '%s', ?);",
            this.postgresDbDriverOptions.getDbSchema(),
            insertOptions.getUserId(),
            insertOptions.getDishName()
        );
        try (
            final PreparedStatement preparedIngredientStatement = this.connection.prepareStatement(ingredientInsert);
        ) {
            final ArrayList<String> ingredientList = insertOptions.getIngredientList();
            for (String ingredient : ingredientList) {
                preparedIngredientStatement.setString(1, ingredient);
                preparedIngredientStatement.addBatch();
            }
            preparedIngredientStatement.executeBatch();
        }
    }

    @Override
    public void deleteDish(final DeleteOptions deleteOptions) throws SQLException {
        this.deleteRecipe(deleteOptions);
    }

    private void deleteRecipe(final DeleteOptions deleteOptions) throws SQLException {
        final String recipeDelete = String.format(
            "DELETE FROM %s.recipe WHERE user_id = %d AND dish_name = '%s';",
            this.postgresDbDriverOptions.getDbSchema(),
            deleteOptions.getUserId(),
            deleteOptions.getDishName()
        );
        try (
            final Statement recipeStatement = this.connection.createStatement();
        ) {
            recipeStatement.executeUpdate(recipeDelete);
        }
    }

    private void deleteIngredients(final DeleteOptions deleteOptions) throws SQLException {
        final String ingredientDelete = String.format(
            "DELETE FROM %s.ingredient WHERE user_id = %d AND dish_name = '%s';",
            this.postgresDbDriverOptions.getDbSchema(),
            deleteOptions.getUserId(),
            deleteOptions.getDishName()
        );
        try (
            final Statement ingredientStatement = this.connection.createStatement();
        ) {
            ingredientStatement.executeUpdate(ingredientDelete);
        }
    }

    // It needs to think
    @Override
    public void updateDish(final UpdateOptions updateOptions) throws SQLException {
        this.updateRecipe(updateOptions);
        this.updateIngredients(updateOptions);
    }

    private void updateRecipe(final UpdateOptions updateOptions) throws SQLException {
        final String recipeUpdate = String.format(
            "UPDATE %s.recipe SET recipe = '%s' WHERE user_id = %d AND dish_name = '%s';",
            this.postgresDbDriverOptions.getDbSchema(),
            updateOptions.getRecipe(),
            updateOptions.getUserId(),
            updateOptions.getDishName()
        );
        try (
            final Statement recipeStatement = this.connection.createStatement();
        ) {
            recipeStatement.executeUpdate(recipeUpdate);
        }
    }

    // It seems to need to delete and then insert again
    // It needs to think about it
    private void updateIngredients(final UpdateOptions updateOptions) throws SQLException {
        final DeleteOptions deleteOptions = new DeleteOptions(
            updateOptions.getUserId(),
            updateOptions.getDishName()
        );
        this.deleteIngredients(deleteOptions);
        final InsertOptions insertOptions = new InsertOptions(
            updateOptions.getUserId(),
            updateOptions.getDishName(),
            updateOptions.getIngredientList(),
            updateOptions.getRecipe()
        );
        this.insertIngredients(insertOptions);
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

    // TODO: Make it more readable
    private void runScript(final String filePath) throws Exception {
        try (
            final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath);
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            final Statement statement = connection.createStatement();
        ) {
            final StringBuilder query = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                if(line.trim().startsWith("-- ")) {
                    continue;
                }
                query.append(line).append(" ");
                if(line.trim().endsWith(";")) {
                    statement.execute(query.toString().trim());
                    query.setLength(0);
                }
            }
        }
    }
}