package utilities;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import mocks.DishMock;
import mocks.MessageMock;
import mocks.UserMock;
import models.commands.CommandStates;
import models.commands.MultiStateCommandTypes;
import models.db.schemas.postgres.PostgresIngredientSchema;
import models.db.schemas.postgres.PostgresRecipeSchema;
import models.db.schemas.postgres.PostgresUserContextSchema;
import models.dtos.UserContextDTO;
import models.exceptions.db.sqlops.NotFoundUserContextException;


final public class CommonUtilities {
    @NonNull
    public static MessageContext getMessageContextMock() {
        final MessageContext messageContext = mock(MessageContext.class);
        final Update update = getUpdateMock();
        final User user = mock(User.class);
        when(messageContext.update()).thenReturn(update);
        when(messageContext.user()).thenReturn(user);
        when(messageContext.chatId()).thenReturn(MessageMock.CHAT_ID);
        when(user.getId()).thenReturn(UserMock.USER_ID);
        return messageContext;
    }

    @NonNull
    public static Update getUpdateMock() {
        final Update update = mock(Update.class);
        final Message message = getMessageMock();
        when(update.getMessage()).thenReturn(message);
        when(message.getText()).thenReturn(MessageMock.TEXT);
        when(update.hasMessage()).thenReturn(true);
        return update;
    }

    @NonNull
    public static Update getUpdateMock(@NonNull final String text) {
        final Update update = mock(Update.class);
        final Message message = getMessageMock();
        when(update.getMessage()).thenReturn(message);
        when(message.getText()).thenReturn(text);
        when(update.hasMessage()).thenReturn(true);
        return update;
    }

    @NonNull
    public static Message getMessageMock() {
        final Message message = mock(Message.class);
        final User user = getUserMock();
        when(message.getFrom()).thenReturn(user);
        when(message.getText()).thenReturn(MessageMock.TEXT);
        when(message.getChatId()).thenReturn(MessageMock.CHAT_ID);
        return message;
    }

    @NonNull
    public static User getUserMock() {
        final User user = mock(User.class);
        when(user.getId()).thenReturn(UserMock.USER_ID);
        return user;
    }

    @NonNull
    public static UserContextDTO getUserContextDTOMock(
        @NonNull final MultiStateCommandTypes multiStateCommandType,
        @NonNull final CommandStates commandState,
        @NonNull final String dishName
    ) throws NotFoundUserContextException, SQLException {
        final ResultSet resultSet = getUserContextDTOResultSetMock(
            multiStateCommandType,
            commandState,
            dishName
        );
        return new UserContextDTO(resultSet);
    }

    @NonNull
    public static ResultSet getUserContextDTOResultSetMock(
        @NonNull final MultiStateCommandTypes multiStateCommandType,
        @NonNull final CommandStates commandState,
        @NonNull final String dishName
    ) throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(PostgresUserContextSchema.MULTI_STATE_COMMAND_TYPE)).thenReturn(multiStateCommandType.getValue());
        when(resultSet.getString(PostgresUserContextSchema.COMMAND_STATE)).thenReturn(commandState.getValue());
        when(resultSet.getString(PostgresUserContextSchema.DISH_NAME)).thenReturn(dishName);
        return resultSet;
    }     
    
    @NonNull
    public static ResultSet getRecipeResultSetMock() throws SQLException {
        final ResultSet recipeResultSet = mock(ResultSet.class);
        when(recipeResultSet.next()).thenReturn(true, false);
        when(recipeResultSet.getString(PostgresRecipeSchema.DISH_NAME)).thenReturn(DishMock.DISH_NAME);
        when(recipeResultSet.getString(PostgresRecipeSchema.RECIPE)).thenReturn(DishMock.RECIPE);
        return recipeResultSet;
    }

    @NonNull
    public static ResultSet getIngredientResultSetMock() throws SQLException {
        final ResultSet ingredientListResultSet = mock(ResultSet.class);
        when(ingredientListResultSet.next()).thenReturn(true, false);
        when(ingredientListResultSet.getString(PostgresIngredientSchema.INGREDIENT)).thenReturn(DishMock.INGREDIENT);
        return ingredientListResultSet;
    }
}
