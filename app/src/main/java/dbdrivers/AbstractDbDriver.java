package dbdrivers;

import java.sql.SQLException;

public abstract class AbstractDbDriver implements AutoCloseable {
    public abstract void connect() throws SQLException;

    public abstract void setup();
 
    @Override
    public abstract void close() throws SQLException;
}