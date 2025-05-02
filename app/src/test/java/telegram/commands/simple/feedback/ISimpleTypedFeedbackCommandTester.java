package telegram.commands.simple.feedback;

import java.sql.Connection;
import java.sql.SQLException;

import org.checkerframework.checker.nullness.qual.NonNull;

import telegram.bot.PovaryoshkaBot;


public interface ISimpleTypedFeedbackCommandTester {
    void feedbackTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception;
}