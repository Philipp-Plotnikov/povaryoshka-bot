package telegram.commands.simple.end.postgres;

import language.ru.BotMessages;
import mocks.MessageMock;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import telegram.bot.PovaryoshkaBot;
import telegram.commands.EndCommand;
import telegram.commands.simple.end.ISimpleTypedEndCommandTester;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import static models.commands.CommandConfig.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static utilities.CommandUtilities.*;
import static utilities.CommonUtilities.*;


public class SimplePostgresEndCommandTester implements ISimpleTypedEndCommandTester {
    @Override
    public void endTest(@NonNull PovaryoshkaBot bot, @NonNull Connection mockedDbConnection) throws SQLException, Exception {
        // Arrange
        final MessageContext messageContext = getMessageContextMock();
        final PreparedStatement deleteUserContextPreparedStatement = mock(PreparedStatement.class);
        when(mockedDbConnection.prepareStatement(any())).thenReturn(deleteUserContextPreparedStatement);

        //Act
        final EndCommand endCommand = getEndDishCommand(bot);
        endCommand.end().action().accept(messageContext);

        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }

    @NonNull
    private EndCommand getEndDishCommand(@NonNull final PovaryoshkaBot bot) throws Exception {
        final Map<String, AbilityExtension> commandMap = bot.getCommandMap();
        if (commandMap == null) {
            throw new Exception("In SimplePostgresDishCommandTester commandMap is null");
        }
        final AbilityExtension untypedCommand = commandMap.get(END_COMMAND_SETTINGS.commandName());
        if (!isEndCommand(untypedCommand)) {
            throw new Exception("In SimplePostgresDishCommandTester endDishCommand is null or is not of expected type");
        }
        return (EndCommand)untypedCommand;
    }
}
