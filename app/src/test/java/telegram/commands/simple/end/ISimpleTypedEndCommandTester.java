package telegram.commands.simple.end;

import org.checkerframework.checker.nullness.qual.NonNull;
import telegram.bot.PovaryoshkaBot;

import java.sql.Connection;
import java.sql.SQLException;


public interface ISimpleTypedEndCommandTester {
    void endTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception;
}
