package telegram.commands.simple.updatedish.postgres;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utilities.CommandUtilities.isUpdateDishCommand;
import static utilities.CommonUtilities.getIngredientResultSetMock;
import static utilities.CommonUtilities.getMessageContextMock;
import static utilities.CommonUtilities.getRecipeResultSetMock;
import static utilities.CommonUtilities.getUpdateMock;
import static utilities.CommonUtilities.getUserContextDTOMock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;

import language.ru.BotMessages;
import language.ru.UserMessages;
import mocks.DishMock;
import mocks.MessageMock;
import models.commands.CommandConfig;
import models.commands.CommandStates;
import models.commands.MultiStateCommandTypes;
import models.dtos.UserContextDTO;
import models.exceptions.db.sqlops.NotFoundUserContextException;
import telegram.bot.PovaryoshkaBot;
import telegram.commands.UpdateDishCommand;
import telegram.commands.simple.updatedish.ISimpleTypedUpdateDishCommandTester;


final public class SimplePostgresUpdateDishCommandTester implements ISimpleTypedUpdateDishCommandTester {
    @Override
    public void updateDishTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final MessageContext messageContext = getMessageContextMock();
        final PreparedStatement recipeListPreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement ingredientListPreparedStatement = mock(PreparedStatement.class);
        final ResultSet recipeResultSet = getRecipeResultSetMock();
        final ResultSet ingredientListResultSet = getIngredientResultSetMock();
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
    
    @Override
    public void handleDishNameStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception {
        // Arrange
        final Update update = getUpdateMock();
        final PreparedStatement recipeListPreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement ingredientListPreparedStatement = mock(PreparedStatement.class);
        final Statement selectDishStatement = mock(Statement.class);
        final ResultSet recipeResultSet = getRecipeResultSetMock();
        final ResultSet ingredientListResultSet = getIngredientResultSetMock();
        final UserContextDTO userContextDTO = getUserContextDTOMock(
            MultiStateCommandTypes.UPDATE,
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
    
    @Override
    public void handleDishNameUpdateConfirmStateYesTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception {
        // Arrange
        final Update update = getUpdateMock(UserMessages.YES);
        final PreparedStatement updateUserContextCommandStatePreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTOMock(
            MultiStateCommandTypes.UPDATE,
            CommandStates.DISH_NAME_UPDATE_CONFIRM,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(updateUserContextCommandStatePreparedStatement);
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleDishNameUpdateConfirmState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), times(1)).send(BotMessages.INPUT_NEW_DISH_NAME, MessageMock.CHAT_ID);
    }

    @Override
    public void handleDishNameUpdateConfirmStateNoTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception {
        // Arrange
        final Update update = getUpdateMock(UserMessages.NO);
        final PreparedStatement updateUserContextCommandStatePreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTOMock(
            MultiStateCommandTypes.UPDATE,
            CommandStates.DISH_NAME_UPDATE_CONFIRM,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(updateUserContextCommandStatePreparedStatement);
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleDishNameUpdateConfirmState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), times(1)).send(BotMessages.CONFIRM_INGREDIENTS_UPDATE, MessageMock.CHAT_ID);
    }

    @Override
    public void handleDishNameUpdateConfirmStateInvalidTextTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception {
        // Arrange
        final Update update = getUpdateMock();
        final PreparedStatement updateUserContextCommandStatePreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTOMock(
            MultiStateCommandTypes.UPDATE,
            CommandStates.DISH_NAME_UPDATE_CONFIRM,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(updateUserContextCommandStatePreparedStatement);
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleDishNameUpdateConfirmState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), times(1)).send(BotMessages.ENTER_YES_OR_NO, MessageMock.CHAT_ID);
    }
    
    @Override
    public void handleDishNameUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception {
        // Arrange
        final Update update = getUpdateMock();
        final PreparedStatement updateDishNamePreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement updateUserContextPreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTOMock(
            MultiStateCommandTypes.UPDATE,
            CommandStates.DISH_NAME_UPDATE,
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
    
    @Override
    public void handleIngredientsUpdateConfirmStateYesTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception {
        // Arrange
        final Update update = getUpdateMock(UserMessages.YES);
        final PreparedStatement updateUserContextCommandStatePreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTOMock(
            MultiStateCommandTypes.UPDATE,
            CommandStates.INGREDIENTS_UPDATE_CONFIRM,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(updateUserContextCommandStatePreparedStatement);
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleIngredientsUpdateConfirmState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), times(1)).send(BotMessages.INPUT_NEW_INGREDIENTS, MessageMock.CHAT_ID);
    }

    @Override
    public void handleIngredientsUpdateConfirmStateNoTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception {
        // Arrange
        final Update update = getUpdateMock(UserMessages.NO);
        final PreparedStatement updateUserContextCommandStatePreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTOMock(
            MultiStateCommandTypes.UPDATE,
            CommandStates.INGREDIENTS_UPDATE_CONFIRM,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(updateUserContextCommandStatePreparedStatement);
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleIngredientsUpdateConfirmState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), times(1)).send(BotMessages.CONFIRM_RECIPE_UPDATE, MessageMock.CHAT_ID);
    }

    @Override
    public void handleIngredientsUpdateConfirmStateInvalidTextTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception {
        // Arrange
        final Update update = getUpdateMock();
        final PreparedStatement updateUserContextCommandStatePreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTOMock(
            MultiStateCommandTypes.UPDATE,
            CommandStates.INGREDIENTS_UPDATE_CONFIRM,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(updateUserContextCommandStatePreparedStatement);
        
        // Act
        final UpdateDishCommand updateDishCommand = getUpdateDishCommand(bot);
        updateDishCommand.handleIngredientsUpdateConfirmState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), times(1)).send(BotMessages.ENTER_YES_OR_NO, MessageMock.CHAT_ID);
    }

    @Override
    public void handleIngredientsUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception {
        // Arrange
        final Update update = getUpdateMock();
        final Statement dishStatement = mock(Statement.class);
        final PreparedStatement deleteIngredientPreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement insertIngredientPreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTOMock(
            MultiStateCommandTypes.UPDATE,
            CommandStates.INGREDIENTS_UPDATE,
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

    @Override
    public void handleRecipeUpdateConfirmStateYesTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) {}

    @Override
    public void handleRecipeUpdateConfirmStateNoTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) {}

    @Override
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
        final AbilityExtension untypedCommand = commandMap.get(CommandConfig.UPDATE_DISH_COMMAND_SETTINGS.commandName());
        if (!isUpdateDishCommand(untypedCommand)) {
            throw new Exception("In SimplePostgresDishCommandTester updateDishCommand is null or is not of expected type");
        }
        return (UpdateDishCommand)untypedCommand;
    }
}
