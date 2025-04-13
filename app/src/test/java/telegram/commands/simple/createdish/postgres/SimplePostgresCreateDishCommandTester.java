package telegram.commands.simple.createdish.postgres;

import static models.commands.CommandConfig.CREATE_DISH_COMMAND_SETTINGS;
import static models.db.schemas.postgres.PostgresUserContextSchema.COMMAND_STATE;
import static models.db.schemas.postgres.PostgresUserContextSchema.DISH_NAME;
import static models.db.schemas.postgres.PostgresUserContextSchema.MULTI_STATE_COMMAND_TYPE;
import static org.junit.Assert.assertEquals;
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
import java.sql.Statement;
import java.util.Map;
import java.util.function.Predicate;

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
import models.db.schemas.postgres.PostgresIngredientSchema;
import models.db.schemas.postgres.PostgresRecipeSchema;
import models.dtos.UserContextDTO;
import models.exceptions.db.sqlops.NotFoundUserContextException;
import telegram.bot.PovaryoshkaBot;
import telegram.commands.CreateDishCommand;
import telegram.commands.simple.createdish.ISimpleTypedCreateDishCommandTester;


final public class SimplePostgresCreateDishCommandTester implements ISimpleTypedCreateDishCommandTester {
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
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }

    public void handleDishNameUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    )throws SQLException, NotFoundUserContextException, Exception {
        // Arrange
        final Update update = getCreateDishUpdate();
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTO(
            MultiStateCommandTypes.CREATE,
            CommandStates.DISH_NAME_UPDATE,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(preparedStatement);
        
        // Act
        final CreateDishCommand createDishCommand = getCreateDishCommand(bot);
        createDishCommand.handleDishNameUpdateState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }

    public void handleIngredientsUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, NotFoundUserContextException, Exception {
        // Arrange
        final Update update = getCreateDishUpdate();
        final PreparedStatement recipeListPreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement ingredientListPreparedStatement = mock(PreparedStatement.class);
        final Statement dishStatement = mock(Statement.class);
        final ResultSet recipeResultSet = getRecipeResultSet();
        final ResultSet ingredientListResultSet = getIngredientResultSet();
        final UserContextDTO userContextDTO = getUserContextDTO(
            MultiStateCommandTypes.CREATE,
            CommandStates.RECIPE_UPDATE,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(
            recipeListPreparedStatement,
            ingredientListPreparedStatement
        );
        when(mockedDbConnection.createStatement()).thenReturn(dishStatement);
        when(recipeListPreparedStatement.executeQuery()).thenReturn(recipeResultSet);
        when(ingredientListPreparedStatement.executeQuery()).thenReturn(ingredientListResultSet);
        
        // Act
        final CreateDishCommand createDishCommand = getCreateDishCommand(bot);
        createDishCommand.handleIngredientsUpdateState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }

    public void handleRecipeUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, NotFoundUserContextException, Exception {
        // Arrange
        final Update update = getCreateDishUpdate();
        final PreparedStatement recipeListPreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement ingredientListPreparedStatement = mock(PreparedStatement.class);
        final Statement selectDishStatement = mock(Statement.class);
        final ResultSet recipeResultSet = getRecipeResultSet();
        final ResultSet ingredientListResultSet = getIngredientResultSet();
        final UserContextDTO userContextDTO = getUserContextDTO(
            MultiStateCommandTypes.CREATE,
            CommandStates.RECIPE_UPDATE,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(
            recipeListPreparedStatement,
            ingredientListPreparedStatement
        );
        when(mockedDbConnection.createStatement()).thenReturn(selectDishStatement);
        when(selectDishStatement.getMoreResults()).thenReturn(true);
        when(selectDishStatement.getResultSet()).thenReturn(
            recipeResultSet,
            ingredientListResultSet
        );
        
        // Act
        final CreateDishCommand createDishCommand = getCreateDishCommand(bot);
        createDishCommand.handleRecipeUpdateState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }

    public void isInCreateDishContextTruthyTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final Update update = getCreateDishUpdate();
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet userContextDTOResultSet = getUserContextDTOResultSet(
            MultiStateCommandTypes.CREATE,
            CommandStates.RECIPE_UPDATE,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(userContextDTOResultSet);
        
        // Act
        final CreateDishCommand createDishCommand = getCreateDishCommand(bot);
        final Predicate<Update> isInCreateDishContextPredicate = createDishCommand.isSpecifiedContext(MultiStateCommandTypes.CREATE);
        final boolean actualValue = isInCreateDishContextPredicate.test(update);
    
        // Assert
        final boolean expectedValue = true;
        assertEquals(expectedValue, actualValue);
    }

    public void isInCreateDishContextFalsyTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final Update update = getCreateDishUpdate();
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet userContextDTOResultSet = getUserContextDTOResultSet(
            MultiStateCommandTypes.GET,
            CommandStates.RECIPE_UPDATE,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(userContextDTOResultSet);
        
        // Act
        final CreateDishCommand createDishCommand = getCreateDishCommand(bot);
        final Predicate<Update> isInCreateDishContextPredicate = createDishCommand.isSpecifiedContext(MultiStateCommandTypes.CREATE);
        final boolean actualValue = isInCreateDishContextPredicate.test(update);
    
        // Assert
        final boolean expectedValue = false;
        assertEquals(expectedValue, actualValue);
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
    private MessageContext getCreateDishMessageContext() {
        final MessageContext messageContext = mock(MessageContext.class);
        final Update update = getCreateDishUpdate();
        final User user = mock(User.class);
        when(messageContext.update()).thenReturn(update);
        when(messageContext.user()).thenReturn(user);
        when(messageContext.chatId()).thenReturn(MessageMock.CHAT_ID);
        when(user.getId()).thenReturn(UserMock.USER_ID);
        return messageContext;
    }

    @NonNull
    private Update getCreateDishUpdate() {
        final Update update = mock(Update.class);
        final Message message = getCreateDishMessage();
        when(update.getMessage()).thenReturn(message);
        when(message.getText()).thenReturn(MessageMock.TEXT);
        when(update.hasMessage()).thenReturn(true);
        return update;
    }

    @NonNull
    private Message getCreateDishMessage() {
        final Message message = mock(Message.class);
        final User user = getCreateDishUser();
        when(message.getFrom()).thenReturn(user);
        when(message.getText()).thenReturn(MessageMock.TEXT);
        when(message.getChatId()).thenReturn(MessageMock.CHAT_ID);
        return message;
    }

    @NonNull
    private User getCreateDishUser() {
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
