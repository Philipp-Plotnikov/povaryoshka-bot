package mocks;

import static models.system.EnvVars.ALTER_SQL_SCRIPT_PATH;
import static models.system.EnvVars.DB_DATABASE;
import static models.system.EnvVars.DB_HOST;
import static models.system.EnvVars.DB_PASSWORD;
import static models.system.EnvVars.DB_PORT;
import static models.system.EnvVars.DB_SCHEMA;
import static models.system.EnvVars.DB_USERNAME;
import static models.system.EnvVars.INIT_SQL_SCRIPT_PATH;
import static models.system.EnvVars.IS_DISTRIBUTED_DATABASE;
import static models.db.drivers.postgres.PostgresConnectionProperties.CURRENT_SCHEMA;
import static models.db.drivers.postgres.PostgresConnectionProperties.PASSWORD;
import static models.db.drivers.postgres.PostgresConnectionProperties.USER;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import models.db.DbTypes;
import models.db.drivers.postgres.PostgresDbDriverOptions;
import models.drivers.TypedDbDriverMocker;

public class DbDriverMocker {
    @Nullable
    private static Map<@NonNull DbTypes, @Nullable TypedDbDriverMocker> dbDriverMockerMap;

    public static MockedStatic<DriverManager> getDbDriverMock(
        @NonNull final DbTypes dbType,
        @NonNull final Connection mockedDbConnection
    ) throws Exception {
        final TypedDbDriverMocker typedDbDriverMocker = dbDriverMockerMap.get(dbType);
        if (typedDbDriverMocker == null) {
            throw new Exception("TypedDbDriverMocker is null");
        }
        return typedDbDriverMocker.getDbDriverMock(mockedDbConnection);
    }

    @NonNull
    private static Map<@NonNull DbTypes, @Nullable TypedDbDriverMocker> getDbDriverMockerMap() {
        final EnumMap<@NonNull DbTypes, @Nullable TypedDbDriverMocker> localDbDriverMockerMap = new EnumMap<>(DbTypes.class);
        localDbDriverMockerMap.put(DbTypes.POSTGRES, DbDriverMocker::postgresDbDriverMocker);
        return Collections.unmodifiableMap(localDbDriverMockerMap);
    }

    @NonNull
    private static MockedStatic<DriverManager> postgresDbDriverMocker(@NonNull final Connection mockedDbConnection) {
        final PostgresDbDriverOptions postgresDbDriverOptions = getPostgresDbDriverOptions();
        Properties postgresConnectionProperties = new Properties();
        setPostgresConnectionProperties(postgresConnectionProperties, postgresDbDriverOptions);
        MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class);
        mockedDriverManager.when(
            () -> DriverManager.getConnection(postgresDbDriverOptions.getDbUrl(), postgresConnectionProperties)
        ).thenReturn(mockedDbConnection);
        return mockedDriverManager;
    }

    @NonNull
    private static PostgresDbDriverOptions getPostgresDbDriverOptions() {
        final PostgresDbDriverOptions postgresDbDriverOptions = new PostgresDbDriverOptions(
            System.getenv(DB_HOST),
            Integer.parseInt(System.getenv(DB_PORT)),
            System.getenv(DB_DATABASE),
            System.getenv(DB_SCHEMA),
            System.getenv(DB_USERNAME),
            System.getenv(DB_PASSWORD),
            System.getenv(INIT_SQL_SCRIPT_PATH),
            System.getenv(ALTER_SQL_SCRIPT_PATH),
            System.getenv(IS_DISTRIBUTED_DATABASE)
        );
        return postgresDbDriverOptions;
    }

    private static void setPostgresConnectionProperties(
        @NonNull final Properties connectionProperties,
        @NonNull final PostgresDbDriverOptions postgresDbDriverOptions
    )
    {
        connectionProperties.setProperty(USER, postgresDbDriverOptions.getDbUsername());
        connectionProperties.setProperty(PASSWORD, postgresDbDriverOptions.getDbPassword());
        connectionProperties.setProperty(CURRENT_SCHEMA, postgresDbDriverOptions.getDbSchema());
    }
}
