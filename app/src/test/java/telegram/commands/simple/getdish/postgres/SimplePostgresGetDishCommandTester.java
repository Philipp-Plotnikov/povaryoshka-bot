package telegram.commands.simple.getdish.postgres;

import language.ru.BotMessages;
import mocks.DishMock;
import mocks.MessageMock;
import models.db.schemas.postgres.PostgresIngredientSchema;
import models.db.schemas.postgres.PostgresRecipeSchema;
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
import java.util.List;
import java.util.Map;

import static mocks.DishMock.*;
import static models.commands.CommandConfig.GET_DISH_COMMAND_SETTINGS;
import static org.junit.Assert.assertEquals;
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
        final PreparedStatement selectRecipeListPreparedStatement = mock(PreparedStatement.class);
        final PreparedStatement selectIngredientListPreparedStatement = mock(PreparedStatement.class);
        final ResultSet recipeListResultSet = getRecipeResultSetMock();
        final ResultSet dishIngredientResultSet = getIngredientResultSetMock();
        final PreparedStatement insertUserContextPreparedStatement = mock(PreparedStatement.class);
        when(mockedDbConnection.prepareStatement(any())).thenReturn(
                selectRecipeListPreparedStatement,
                selectIngredientListPreparedStatement,
                insertUserContextPreparedStatement
        );
        when(selectRecipeListPreparedStatement.executeQuery()).thenReturn(recipeListResultSet);
        when(recipeListResultSet.next()).thenReturn(true, false);
        when(recipeListResultSet.getString(PostgresRecipeSchema.DISH_NAME)).thenReturn(DishMock.DISH_NAME);
        when(recipeListResultSet.getString(PostgresRecipeSchema.RECIPE)).thenReturn(DishMock.RECIPE);
        when(selectIngredientListPreparedStatement.executeQuery()).thenReturn(dishIngredientResultSet);
        when(dishIngredientResultSet.next()).thenReturn(true, false);
        when(dishIngredientResultSet.getString(PostgresIngredientSchema.INGREDIENT)).thenReturn(DishMock.INGREDIENT);

        //Act
        final GetDishCommand getDishCommand = getGetDishCommand(bot);
        getDishCommand.getDish().action().accept(messageContext);

        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
    }

    @Override
    public void getFormatDishInfoTest(@NonNull PovaryoshkaBot bot, @NonNull Connection mockedDbConnection) throws SQLException, Exception {
        // Arrange
        final DishDTO dishDTO = new DishDTO(
            DISH_NAME,
            List.of(INGREDIENT),
            RECIPE
        );

        //Act
        final GetDishCommand getDishCommand = getGetDishCommand(bot);
        final String expectedValue = FORMAT_DISH_INFO;
        final String actualValue = getDishCommand.getFormatDishInfo(dishDTO);

        // Assert
        assertEquals(expectedValue, actualValue);
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
