package telegram.commands.simple.createdish.postgres;

import static models.commands.CommandConfig.CREATE_DISH_COMMAND_SETTINGS;
import static models.db.schemas.postgres.PostgresUserContextSchema.COMMAND_STATE;
import static models.db.schemas.postgres.PostgresUserContextSchema.DISH_NAME;
import static models.db.schemas.postgres.PostgresUserContextSchema.MULTI_STATE_COMMAND_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utilities.CommandUtilities.isCreateDishCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import language.ru.BotMessages;
import mocks.DishMock;
import mocks.MessageMock;
import mocks.UserMock;
import models.commands.CommandStates;
import models.commands.MultiStateCommandTypes;
import telegram.bot.PovaryoshkaBot;
import telegram.commands.CreateDishCommand;
import telegram.commands.simple.createdish.ISimpleTypedCreateDishCommandTester;


final public class SimplePostgresDishCommandTester implements ISimpleTypedCreateDishCommandTester {
    public void createDishTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final MessageContext messageContext = getCreateDishMessageContext();
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(mockedDbConnection.prepareStatement(any())).thenReturn(preparedStatement);
        
        // Act
        final CreateDishCommand createDishCommand = getCreateDishCommand(bot);
        createDishCommand.createDish().action().accept(messageContext);
    
        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, messageContext.update().getUpdateId());
    }

    public void handleDishNameUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    )throws SQLException, Exception {
        // Arrange
        final MessageContext messageContext = getCreateDishMessageContext();
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet userContextDTOResultSet = getUserContextDTOResultSet(
            MultiStateCommandTypes.CREATE,
            CommandStates.DISH_NAME_UPDATE,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(userContextDTOResultSet);
        
        // Act
        final CreateDishCommand createDishCommand = getCreateDishCommand(bot);
        createDishCommand.createDish().action().accept(messageContext);
    
        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, messageContext.update().getUpdateId());
    }

    public void handleIngredientsUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) {

    }

    public void handleRecipeUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) {

    }

    public void isInCreateDishContextTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) {

    }

    @NonNull
    private MessageContext getCreateDishMessageContext() {
        final MessageContext messageContext = mock(MessageContext.class);
        final Update update = mock(Update.class);
        final User user = mock(User.class);
        final Message message = mock(Message.class);
        when(messageContext.update()).thenReturn(update);
        when(messageContext.user()).thenReturn(user);
        when(user.getId()).thenReturn(UserMock.USER_ID);
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(MessageMock.CHAT_ID);
        return messageContext;
    }

    @NonNull
    private CreateDishCommand getCreateDishCommand(@NonNull final PovaryoshkaBot bot) throws Exception {
        final Map<String, AbilityExtension> commandMap = bot.getCommandMap();
        if (commandMap == null) {
            throw new Exception("In SimplePostgresDishCommandTester commandMap is null");
        }
        final AbilityExtension untypedCommand = commandMap.get(CREATE_DISH_COMMAND_SETTINGS.commandName());
        if (!isCreateDishCommand(untypedCommand)) {
            throw new Exception("In SimplePostgresDishCommandTester createDishCommand is null or is not of expected type");
        }
        return (CreateDishCommand)untypedCommand;
    }

    @NonNull
    private ResultSet getUserContextDTOResultSet(
        @NonNull final MultiStateCommandTypes multiStateCommandType,
        @NonNull final CommandStates commandState,
        @NonNull final String dishName
    ) throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(MULTI_STATE_COMMAND_TYPE)).thenReturn(multiStateCommandType.getValue());
        when(resultSet.getString(COMMAND_STATE)).thenReturn(commandState.getValue());
        when(resultSet.getString(DISH_NAME)).thenReturn(dishName);
        return resultSet;
    }
}
