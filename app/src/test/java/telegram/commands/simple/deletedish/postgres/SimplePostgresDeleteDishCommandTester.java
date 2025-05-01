package telegram.commands.simple.deletedish.postgres;

import language.ru.BotMessages;
import mocks.DishMock;
import mocks.MessageMock;
import models.db.schemas.postgres.PostgresIngredientSchema;
import models.db.schemas.postgres.PostgresRecipeSchema;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import telegram.bot.PovaryoshkaBot;
import telegram.commands.DeleteDishCommand;
import telegram.commands.simple.deletedish.ISimpleTypedDeleteDishCommandTester;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static models.commands.CommandConfig.DELETE_DISH_COMMAND_SETTINGS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static utilities.CommandUtilities.isDeleteDishCommand;
import static utilities.CommonUtilities.*;


public class SimplePostgresDeleteDishCommandTester implements ISimpleTypedDeleteDishCommandTester {
    @Override
    public void deleteDishTest(@NonNull PovaryoshkaBot bot, @NonNull Connection mockedDbConnection) throws SQLException, Exception {
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
        final DeleteDishCommand getDishCommand = getDeleteDishCommand(bot);
        getDishCommand.deleteDish().action().accept(messageContext);

        // Assert
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, MessageMock.CHAT_ID);
        }

    @NonNull
    private DeleteDishCommand getDeleteDishCommand(@NonNull final PovaryoshkaBot bot) throws Exception {
        final Map<String, AbilityExtension> commandMap = bot.getCommandMap();
        if (commandMap == null) {
            throw new Exception("In SimplePostgresDishCommandTester commandMap is null");
        }
        final AbilityExtension untypedCommand = commandMap.get(DELETE_DISH_COMMAND_SETTINGS.commandName());
        if (!isDeleteDishCommand(untypedCommand)) {
            throw new Exception("In SimplePostgresDishCommandTester deleteDishCommand is null or is not of expected type");
        }
        return (DeleteDishCommand)untypedCommand;
    }
}
