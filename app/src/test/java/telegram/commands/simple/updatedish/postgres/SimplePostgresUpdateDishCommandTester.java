package telegram.commands.simple.updatedish.postgres;

import static models.commands.CommandConfig.UPDATE_DISH_COMMAND_SETTINGS;
import static models.commands.CommandStates.DISH_NAME_UPDATE;
import static models.commands.CommandStates.DISH_NAME_UPDATE_CONFIRM;
import static models.commands.CommandStates.INGREDIENTS_UPDATE;
import static models.commands.CommandStates.INGREDIENTS_UPDATE_CONFIRM;
import static models.commands.MultiStateCommandTypes.UPDATE;
import static models.db.schemas.postgres.PostgresUserContextSchema.COMMAND_STATE;
import static models.db.schemas.postgres.PostgresUserContextSchema.DISH_NAME;
import static models.db.schemas.postgres.PostgresUserContextSchema.MULTI_STATE_COMMAND_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utilities.CommandUtilities.isUpdateDishCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.bouncycastle.jcajce.provider.asymmetric.EXTERNAL;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import language.ru.BotMessages;
import language.ru.UserMessages;
import mocks.DishMock;
import mocks.MessageMock;
import mocks.UserMock;
import models.commands.CommandStates;
import models.commands.MultiStateCommandTypes;
import models.db.schemas.postgres.PostgresIngredientSchema;
import models.db.schemas.postgres.PostgresRecipeSchema;
import models.dtos.UserContextDTO;
import models.exceptions.db.sqlops.NotFoundUserContextException;
import telegram.bot.PovaryoshkaBot;
import telegram.commands.UpdateDishCommand;
import telegram.commands.simple.updatedish.ISimpleTypedUpdateDishCommandTester;


final public class SimplePostgresUpdateDishCommandTester implements ISimpleTypedUpdateDishCommandTester {
    public void updateDishTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final MessageContext messageContext = getUpdateDishMessageContext();
        final PreparedStatement recipeListPreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement ingredientListPreparedStatement = mock(PreparedStatement.class);
        final ResultSet recipeResultSet = getRecipeResultSet();
        final ResultSet ingredientListResultSet = getIngredientResultSet();
        when(mockedDbConnection.prepareStatement(any())).thenReturn(
            recipeListPreparedStatement,
            ingredientListPreparedStatement
        );
        when(recipeListPreparedStatement.executeQuery()).thenReturn(recipeResultSet);
        when(ingredientListPreparedStatement.executeQuery()).thenReturn(ingredientListResultSet);
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.updateDish().action().accept(messageContext);
    
        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }
    
    public void handleDishNameStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final Update update = getUpdateDishUpdate();
        final PreparedStatement recipeListPreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement ingredientListPreparedStatement = mock(PreparedStatement.class);
        final Statement selectDishStatement = mock(Statement.class);
        final ResultSet recipeResultSet = getRecipeResultSet();
        final ResultSet ingredientListResultSet = getIngredientResultSet();
        final UserContextDTO userContextDTO = getUserContextDTO(
            UPDATE,
            CommandStates.DISH_NAME,
            DishMock.DISH_NAME
        );
        final PreparedStatement updateUserContextPreparedStatement = mock(PreparedStatement.class);
        when(mockedDbConnection.prepareStatement(any())).thenReturn(
            recipeListPreparedStatement,
            ingredientListPreparedStatement,
            updateUserContextPreparedStatement
        );
        when(mockedDbConnection.createStatement()).thenReturn(selectDishStatement);
        when(selectDishStatement.getMoreResults()).thenReturn(true);
        when(selectDishStatement.getResultSet()).thenReturn(
            recipeResultSet,
            ingredientListResultSet
        );
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleDishNameState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }
    
    public void handleDishNameUpdateConfirmStateYesTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final Update update = getUpdateDishUpdate(UserMessages.YES);
        final PreparedStatement updateUserContextCommandStatePreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTO(
            UPDATE,
            DISH_NAME_UPDATE_CONFIRM,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(updateUserContextCommandStatePreparedStatement);
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleDishNameUpdateConfirmState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), times(1)).send(BotMessages.INPUT_NEW_DISH_NAME, MessageMock.CHAT_ID);
    }

    public void handleDishNameUpdateConfirmStateNoTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final Update update = getUpdateDishUpdate(UserMessages.NO);
        final PreparedStatement updateUserContextCommandStatePreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTO(
            UPDATE,
            DISH_NAME_UPDATE_CONFIRM,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(updateUserContextCommandStatePreparedStatement);
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleDishNameUpdateConfirmState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), times(1)).send(BotMessages.CONFIRM_INGREDIENTS_UPDATE, MessageMock.CHAT_ID);
    }

    public void handleDishNameUpdateConfirmStateInvalidTextTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final Update update = getUpdateDishUpdate();
        final PreparedStatement updateUserContextCommandStatePreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTO(
            UPDATE,
            DISH_NAME_UPDATE_CONFIRM,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(updateUserContextCommandStatePreparedStatement);
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleDishNameUpdateConfirmState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), times(1)).send(BotMessages.ENTER_YES_OR_NO, MessageMock.CHAT_ID);
    }
    
    public void handleDishNameUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final Update update = getUpdateDishUpdate();
        final PreparedStatement updateDishNamePreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement updateUserContextPreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTO(
            UPDATE,
            DISH_NAME_UPDATE,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(
            updateDishNamePreparedStatement,
            updateUserContextPreparedStatement
        );
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleDishNameUpdateState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }
    
    public void handleIngredientsUpdateConfirmStateYesTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final Update update = getUpdateDishUpdate(UserMessages.YES);
        final PreparedStatement updateUserContextCommandStatePreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTO(
            UPDATE,
            INGREDIENTS_UPDATE_CONFIRM,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(updateUserContextCommandStatePreparedStatement);
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleIngredientsUpdateConfirmState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), times(1)).send(BotMessages.INPUT_NEW_INGREDIENTS, MessageMock.CHAT_ID);
    }

    public void handleIngredientsUpdateConfirmStateNoTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final Update update = getUpdateDishUpdate(UserMessages.NO);
        final PreparedStatement updateUserContextCommandStatePreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTO(
            UPDATE,
            INGREDIENTS_UPDATE_CONFIRM,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(updateUserContextCommandStatePreparedStatement);
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleIngredientsUpdateConfirmState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), times(1)).send(BotMessages.CONFIRM_RECIPE_UPDATE, MessageMock.CHAT_ID);
    }

    public void handleIngredientsUpdateConfirmStateInvalidTextTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final Update update = getUpdateDishUpdate();
        final PreparedStatement updateUserContextCommandStatePreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTO(
            UPDATE,
            INGREDIENTS_UPDATE_CONFIRM,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(updateUserContextCommandStatePreparedStatement);
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleIngredientsUpdateConfirmState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), times(1)).send(BotMessages.ENTER_YES_OR_NO, MessageMock.CHAT_ID);
    }

    public void handleIngredientsUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final Update update = getUpdateDishUpdate();
        final Statement dishStatement = mock(Statement.class);
        final PreparedStatement deleteIngredientPreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement insertIngredientPreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTO(
            UPDATE,
            INGREDIENTS_UPDATE,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.createStatement()).thenReturn(dishStatement);
        when(mockedDbConnection.prepareStatement(any())).thenReturn(
            deleteIngredientPreparedStatement,
            insertIngredientPreparedStatement
        );
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleIngredientsUpdateState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }

    public void handleRecipeUpdateConfirmStateYesTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) {}

    public void handleRecipeUpdateConfirmStateNoTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) {}

    public void handleRecipeUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) {}

    @NonNull
    private UpdateDishCommand getUpdateDishCommand(@NonNull final PovaryoshkaBot bot) throws Exception {
        final Map<String, AbilityExtension> commandMap = bot.getCommandMap();
        if (commandMap == null) {
            throw new Exception("In SimplePostgresDishCommandTester commandMap is null");
        }
        final AbilityExtension untypedCommand = commandMap.get(UPDATE_DISH_COMMAND_SETTINGS.commandName());
        if (!isUpdateDishCommand(untypedCommand)) {
            throw new Exception("In SimplePostgresDishCommandTester updateDishCommand is null or is not of expected type");
        }
        return (UpdateDishCommand)untypedCommand;
    }

     @NonNull
    private MessageContext getUpdateDishMessageContext() {
        final MessageContext messageContext = mock(MessageContext.class);
        final Update update = getUpdateDishUpdate();
        final User user = mock(User.class);
        when(messageContext.update()).thenReturn(update);
        when(messageContext.user()).thenReturn(user);
        when(messageContext.chatId()).thenReturn(MessageMock.CHAT_ID);
        when(user.getId()).thenReturn(UserMock.USER_ID);
        return messageContext;
    }

    @NonNull
    private Update getUpdateDishUpdate() {
        final Update update = mock(Update.class);
        final Message message = getUpdateDishMessage();
        when(update.getMessage()).thenReturn(message);
        when(message.getText()).thenReturn(MessageMock.TEXT);
        when(update.hasMessage()).thenReturn(true);
        return update;
    }

    @NonNull
    private Update getUpdateDishUpdate(@NonNull final String text) {
        final Update update = mock(Update.class);
        final Message message = getUpdateDishMessage();
        when(update.getMessage()).thenReturn(message);
        when(message.getText()).thenReturn(text);
        when(update.hasMessage()).thenReturn(true);
        return update;
    }

    @NonNull
    private Message getUpdateDishMessage() {
        final Message message = mock(Message.class);
        final User user = getUpdateDishUser();
        when(message.getFrom()).thenReturn(user);
        when(message.getText()).thenReturn(MessageMock.TEXT);
        when(message.getChatId()).thenReturn(MessageMock.CHAT_ID);
        return message;
    }

    @NonNull
    private User getUpdateDishUser() {
        final User user = mock(User.class);
        when(user.getId()).thenReturn(UserMock.USER_ID);
        return user;
    }

    @NonNull
    private UserContextDTO getUserContextDTO(
        @NonNull final MultiStateCommandTypes multiStateCommandType,
        @NonNull final CommandStates commandState,
        @NonNull final String dishName
    ) throws SQLException, NotFoundUserContextException {
        final ResultSet resultSet = getUserContextDTOResultSet(
            multiStateCommandType,
            commandState,
            dishName
        );
        return new UserContextDTO(resultSet);
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

    @NonNull
    private ResultSet getRecipeResultSet() throws SQLException {
        final ResultSet recipeResultSet = mock(ResultSet.class);
        when(recipeResultSet.next()).thenReturn(true, false);
        when(recipeResultSet.getString(PostgresRecipeSchema.DISH_NAME)).thenReturn(DishMock.DISH_NAME);
        when(recipeResultSet.getString(PostgresRecipeSchema.RECIPE)).thenReturn(DishMock.RECIPE);
        return recipeResultSet;
    }

    @NonNull
    private ResultSet getIngredientResultSet() throws SQLException {
        final ResultSet ingredientListResultSet = mock(ResultSet.class);
        when(ingredientListResultSet.next()).thenReturn(true, false);
        when(ingredientListResultSet.getString(PostgresIngredientSchema.INGREDIENT)).thenReturn(DishMock.INGREDIENT);
        return ingredientListResultSet;
    }
}
