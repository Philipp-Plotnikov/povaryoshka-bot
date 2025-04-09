package telegram.commands.simple.updatedish.postgres;

import static models.commands.CommandConfig.UPDATE_DISH_COMMAND_SETTINGS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utilities.CommandUtilities.isUpdateDishCommand;

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
import mocks.UpdateMock;
import mocks.UserMock;
import models.db.schemas.postgres.PostgresIngredientSchema;
import models.db.schemas.postgres.PostgresRecipeSchema;
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
        verify(bot.getSilent(), never()).send(BotMessages.SOMETHING_WENT_WRONG, messageContext.update().getUpdateId());
    }

    @NonNull
    private MessageContext getUpdateDishMessageContext() {
        final MessageContext messageContext = mock(MessageContext.class);
        final Update update = mock(Update.class);
        final User user = mock(User.class);
        final Message message = mock(Message.class);
        when(messageContext.update()).thenReturn(update);
        when(messageContext.user()).thenReturn(user);
        when(user.getId()).thenReturn(UserMock.USER_ID);
        when(update.getMessage()).thenReturn(message);
        when(update.getUpdateId()).thenReturn(UpdateMock.UPDATE_ID);
        when(message.getChatId()).thenReturn(MessageMock.CHAT_ID);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(MessageMock.TEXT);
        return messageContext;
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
    
    public void handleDishNameStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) {
        
    }
    
    public void handleDishNameUpdateConfirmStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) {}
    
    public void handleDishNameUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) {}
    
    public void handleIngredientsUpdateConfirmStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) {}

    public void handleIngredientsUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) {}

    public void handleRecipeUpdateConfirmStateTest(
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
}
