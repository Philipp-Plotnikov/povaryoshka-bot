package mocks;

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
import utilities.PostgresDbDriverUtilities;


public class DbDriverMocker {
    @Nullable
    private static Map<@NonNull DbTypes, @Nullable TypedDbDriverMocker> dbDriverMockerMap;

    @NonNull
    public static MockedStatic<DriverManager> getDbDriverMock(
        @NonNull final DbTypes dbType,
        @NonNull final Connection mockedDbConnection
    ) throws Exception {
        dbDriverMockerMap = getDbDriverMockerMap();
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
        final PostgresDbDriverOptions postgresDbDriverOptions = PostgresDbDriverUtilities.getPostgresDbDriverOptions();
        final Properties postgresConnectionProperties = new Properties();
        setPostgresConnectionProperties(postgresConnectionProperties, postgresDbDriverOptions);
        final MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class);
        mockedDriverManager.when(
            () -> DriverManager.getConnection(postgresDbDriverOptions.getDbUrl(), postgresConnectionProperties)
        ).thenReturn(mockedDbConnection);
        return mockedDriverManager;
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
