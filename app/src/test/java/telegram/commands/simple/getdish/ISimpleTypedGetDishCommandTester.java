package telegram.commands.simple.getdish;

import org.checkerframework.checker.nullness.qual.NonNull;
import telegram.bot.PovaryoshkaBot;

import java.sql.Connection;
import java.sql.SQLException;


public interface ISimpleTypedGetDishCommandTester {
    void getDishTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception;

    void getFormatDishInfoTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception;
}
