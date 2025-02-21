package models.drivers;

import java.sql.Connection;
import java.sql.DriverManager;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.mockito.MockedStatic;

@FunctionalInterface
public interface TypedDbDriverMocker {
    @NonNull
    MockedStatic<DriverManager> getDbDriverMock(@NonNull final Connection dbConnection);
}