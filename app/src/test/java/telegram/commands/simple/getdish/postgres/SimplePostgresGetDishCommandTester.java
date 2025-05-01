package telegram.commands.simple.getdish.postgres;

import language.ru.BotMessages;
import mocks.MessageMock;
import models.dtos.DishDTO;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import telegram.bot.PovaryoshkaBot;
import telegram.commands.GetDishCommand;
import telegram.commands.simple.getdish.ISimpleTypedGetDishCommandTester;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static models.commands.CommandConfig.GET_DISH_COMMAND_SETTINGS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static utilities.CommandUtilities.*;
import static utilities.CommonUtilities.*;

public class SimplePostgresGetDishCommandTester  implements ISimpleTypedGetDishCommandTester {
    @Override
    public void getDishTest(@NonNull PovaryoshkaBot bot, @NonNull Connection mockedDbConnection) throws SQLException, Exception {
        // Arrange
        final MessageContext messageContext = getMessageContextMock();
        final PreparedStatement insertUserContextPreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement recipeListPreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement ingredientListPreparedStatement = mock(PreparedStatement.class);
        final ResultSet recipeResultSet = getRecipeResultSetMock();
        final ResultSet ingredientListResultSet = getIngredientResultSetMock();
        when(mockedDbConnection.prepareStatement(any())).thenReturn(insertUserContextPreparedStatement);
        when(mockedDbConnection.prepareStatement(any())).thenReturn(
                recipeListPreparedStatement,
                ingredientListPreparedStatement
        );
        when(recipeListPreparedStatement.executeQuery()).thenReturn(recipeResultSet);
        when(ingredientListPreparedStatement.executeQuery()).thenReturn(ingredientListResultSet);

        //Act
        final GetDishCommand getDishCommand = getGetDishCommand(bot);
        getDishCommand.getDish().action().accept(messageContext);

        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }

    @Override
    public void getFormatDishInfoTest(@NonNull PovaryoshkaBot bot, @NonNull Connection mockedDbConnection) throws SQLException, Exception {
        // Arrange
        final PreparedStatement insertUserContextPreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement recipeListPreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement ingredientListPreparedStatement = mock(PreparedStatement.class);
        final ResultSet recipeResultSet = getRecipeResultSetMock();
        final ResultSet ingredientListResultSet = getIngredientResultSetMock();
        final DishDTO dishDTO = getDishDTOMock();
        when(mockedDbConnection.prepareStatement(any())).thenReturn(insertUserContextPreparedStatement);
        when(mockedDbConnection.prepareStatement(any())).thenReturn(
                recipeListPreparedStatement,
                ingredientListPreparedStatement
        );
        when(recipeListPreparedStatement.executeQuery()).thenReturn(recipeResultSet);
        when(ingredientListPreparedStatement.executeQuery()).thenReturn(ingredientListResultSet);

        //Act
        final GetDishCommand getDishCommand = getGetDishCommand(bot);
        getDishCommand.getFormatDishInfo(dishDTO);

        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }

    @NonNull
    private GetDishCommand getGetDishCommand(@NonNull final PovaryoshkaBot bot) throws Exception {
        final Map<String, AbilityExtension> commandMap = bot.getCommandMap();
        if (commandMap == null) {
            throw new Exception("In SimplePostgresDishCommandTester commandMap is null");
        }
        final AbilityExtension untypedCommand = commandMap.get(GET_DISH_COMMAND_SETTINGS.commandName());
        if (!isGetDishCommand(untypedCommand)) {
            throw new Exception("In SimplePostgresDishCommandTester getDishCommand is null or is not of expected type");
        }
        return (GetDishCommand)untypedCommand;
    }
}
