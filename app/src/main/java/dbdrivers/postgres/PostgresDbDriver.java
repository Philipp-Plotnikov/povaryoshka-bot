package dbdrivers.postgres;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import dbdrivers.AbstractDbDriver;
import models.dbdrivers.postgres.PostgresDbDriverOptions;

public class PostgresDbDriver extends AbstractDbDriver {
    private final PostgresDbDriverOptions options;

    private Connection connection;

    public PostgresDbDriver(final PostgresDbDriverOptions options) {
        this.options = options;       
    }

    @Override
    public void connect() throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.setProperty("user", this.options.getDbUsername());
        connectionProps.setProperty("password", this.options.getDbPassword());
        connectionProps.setProperty("currentSchema", this.options.getDbSchema());
        this.connection = DriverManager.getConnection(this.options.getDbUrl(), connectionProps);
    }

    /*
        1. Create or update tables
     */
    @Override
    public void setup() {
        this.runInitScripts();
        this.runAlterScripts();
    }

    @Override
    public void close() throws SQLException {
        this.connection.close();
    }

    private void runInitScripts() {

    }

    private void runAlterScripts() {

    }

    private void runScript(final String filePath) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(
            new FileReader(filePath)
        );
        Statement statement = connection.createStatement();
        StringBuilder query = new StringBuilder();
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