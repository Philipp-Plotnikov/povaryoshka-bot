package telegram.commands.simple.feedback.postgres;

import static models.commands.CommandConfig.FEEDBACK_COMMAND_SETTINGS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utilities.CommandUtilities.isFeedbackCommand;
import static utilities.CommonUtilities.getMessageContextMock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import language.ru.BotMessages;
import mocks.MessageMock;
import telegram.bot.PovaryoshkaBot;
import telegram.commands.FeedbackCommand;
import telegram.commands.simple.feedback.ISimpleTypedFeedbackCommandTester;


public class SimplePostgresFeedbackCommandTester implements ISimpleTypedFeedbackCommandTester {
    public void feedbackTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final MessageContext messageContext = getMessageContextMock();
        final PreparedStatement insertUserContextPreparedStatement = mock(PreparedStatement.class);
        when(mockedDbConnection.prepareStatement(any())).thenReturn(insertUserContextPreparedStatement);

        //Act
        final FeedbackCommand feedbackCommand = getFeedbackCommand(bot);
        feedbackCommand.feedback().action().accept(messageContext);

        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }

    @NonNull
    private FeedbackCommand getFeedbackCommand(@NonNull final PovaryoshkaBot bot) throws Exception {
        final Map<String, AbilityExtension> commandMap = bot.getCommandMap();
        if (commandMap == null) {
            throw new Exception("In SimplePostgresDishCommandTester commandMap is null");
        }
        final AbilityExtension untypedCommand = commandMap.get(FEEDBACK_COMMAND_SETTINGS.commandName());
        if (!isFeedbackCommand(untypedCommand)) {
            throw new Exception("In SimplePostgresDishCommandTester feedbackDishCommand is null or is not of expected type");
        }
        return (FeedbackCommand)untypedCommand;
    }
}
