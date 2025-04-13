package telegram.commands.simple.createdish.postgres;

import static models.commands.CommandConfig.CREATE_DISH_COMMAND_SETTINGS;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utilities.CommandUtilities.isCreateDishCommand;
import static utilities.CommonUtilities.getMessageContextMock;
import static utilities.CommonUtilities.getUpdateMock;
import static utilities.CommonUtilities.getUserContextDTOMock;
import static utilities.CommonUtilities.getUserContextDTOResultSetMock;
import static utilities.CommonUtilities.getRecipeResultSetMock;
import static utilities.CommonUtilities.getIngredientResultSetMock;

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

import language.ru.BotMessages;
import mocks.DishMock;
import mocks.MessageMock;
import models.commands.CommandStates;
import models.commands.MultiStateCommandTypes;
import models.dtos.UserContextDTO;
import models.exceptions.db.sqlops.NotFoundUserContextException;
import telegram.bot.PovaryoshkaBot;
import telegram.commands.CreateDishCommand;
import telegram.commands.simple.createdish.ISimpleTypedCreateDishCommandTester;


final public class SimplePostgresCreateDishCommandTester implements ISimpleTypedCreateDishCommandTester {
    @Override
    public void createDishTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final MessageContext messageContext = getMessageContextMock();
        final PreparedStatement insertUserContextPreparedStatement = mock(PreparedStatement.class);
        when(mockedDbConnection.prepareStatement(any())).thenReturn(insertUserContextPreparedStatement);
        
        // Act
        final CreateDishCommand createDishCommand = getCreateDishCommand(bot);
        createDishCommand.createDish().action().accept(messageContext);
    
        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }

    @Override
    public void handleDishNameUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    )throws NotFoundUserContextException, SQLException, Exception {
        // Arrange
        final Update update = getUpdateMock();
        final Statement dishStatement = mock(Statement.class);
        final PreparedStatement insertRecipePreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement insertIngredientPreparedStatement = mock(PreparedStatement.class);
        final UserContextDTO userContextDTO = getUserContextDTOMock(
            MultiStateCommandTypes.CREATE,
            CommandStates.DISH_NAME_UPDATE,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.createStatement()).thenReturn(dishStatement);
        when(mockedDbConnection.prepareStatement(any())).thenReturn(
            insertRecipePreparedStatement,
            insertIngredientPreparedStatement
        );
        
        // Act
        final CreateDishCommand createDishCommand = getCreateDishCommand(bot);
        createDishCommand.handleDishNameUpdateState(update, userContextDTO);
    
        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }

    @Override
    public void handleIngredientsUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception {
        // Arrange
        final Update update = getUpdateMock();
        final PreparedStatement recipeListPreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement ingredientListPreparedStatement = mock(PreparedStatement.class);
        final Statement dishStatement = mock(Statement.class);
        final ResultSet recipeResultSet = getRecipeResultSetMock();
        final ResultSet ingredientListResultSet = getIngredientResultSetMock();
        final UserContextDTO userContextDTO = getUserContextDTOMock(
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

    @Override
    public void handleRecipeUpdateStateTest(
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

    @Override
    public void isInCreateDishContextTruthyTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final Update update = getUpdateMock();
        final PreparedStatement selectUserContextPreparedStatement = mock(PreparedStatement.class);
        final ResultSet userContextDTOResultSet = getUserContextDTOResultSetMock(
            MultiStateCommandTypes.CREATE,
            CommandStates.RECIPE_UPDATE,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(selectUserContextPreparedStatement);
        when(selectUserContextPreparedStatement.executeQuery()).thenReturn(userContextDTOResultSet);
        
        // Act
        final CreateDishCommand createDishCommand = getCreateDishCommand(bot);
        final Predicate<Update> isInCreateDishContextPredicate = createDishCommand.isSpecifiedContext(MultiStateCommandTypes.CREATE);
        final boolean actualValue = isInCreateDishContextPredicate.test(update);
    
        // Assert
        final boolean expectedValue = true;
        assertEquals(expectedValue, actualValue);
    }

    @Override
    public void isInCreateDishContextFalsyTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception {
        // Arrange
        final Update update = getUpdateMock();
        final PreparedStatement selectUserContextPreparedStatement = mock(PreparedStatement.class);
        final ResultSet userContextDTOResultSet = getUserContextDTOResultSetMock(
            MultiStateCommandTypes.GET,
            CommandStates.RECIPE_UPDATE,
            DishMock.DISH_NAME
        );
        when(mockedDbConnection.prepareStatement(any())).thenReturn(selectUserContextPreparedStatement);
        when(selectUserContextPreparedStatement.executeQuery()).thenReturn(userContextDTOResultSet);
        
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
}
