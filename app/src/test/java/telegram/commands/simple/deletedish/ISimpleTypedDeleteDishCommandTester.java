package telegram.commands.simple.deletedish;

import org.checkerframework.checker.nullness.qual.NonNull;
import telegram.bot.PovaryoshkaBot;

import java.sql.Connection;
import java.sql.SQLException;


public interface ISimpleTypedDeleteDishCommandTester {
    void deleteDishTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception;
}
