package telegram.commands;

import models.db.sqlops.dish.DishDeleteOptions;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import models.db.sqlops.usercontext.UserContextInsertOptions;
import models.exceptions.db.sqlops.NotFoundDishException;

import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;

import static models.commands.CommandStates.DISH_NAME;
import static models.commands.MultiStateCommandTypes.DELETE;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

import org.checkerframework.checker.nullness.qual.NonNull;

import static models.commands.CommandConfig.DELETE_DISH_COMMAND_SETTINGS;

import org.telegram.telegrambots.meta.api.objects.Update;

import language.ru.BotMessages;
import telegram.bot.PovaryoshkaBot;

import java.sql.SQLException;


public class DeleteDishCommand extends AbstractCommand {

    public DeleteDishCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
    }

    @NonNull
    public Ability deleteDish() {
        return Ability.builder()
            .name(DELETE_DISH_COMMAND_SETTINGS.commandName())
            .info(DELETE_DISH_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL)
            .action(ctx -> {
                final Update update = ctx.update();
                try {
                    final String message = getUserDishList(ctx);
                    if (message == null) {
                        sendSilently(BotMessages.USER_DOES_NOT_HAVE_DISHES, update);
                        return;
                    }
                    sendSilently(message, update);
                    sendSilently(BotMessages.WRITE_DISH_NAME_FROM_LIST_TO_DELETE, update);
                    dbDriver.insertUserContext(
                        new UserContextInsertOptions(
                            ctx.user().getId(),
                            DELETE,
                            DISH_NAME,
                            null
                        )
                    );
                } catch(SQLException e) {
                    sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                    System.out.println("Ошибка при вставке: " + e.getMessage());
                }
            })
            .reply((action, update) -> {
                    try {
                        final long userId = update.getMessage().getFrom().getId();
                        final String dishName = update.getMessage().getText().trim();
                        dbDriver.executeAsTransaction(
                            () -> {
                                dbDriver.deleteDish(
                                    new DishDeleteOptions(
                                        userId,
                                        dishName
                                    )
                                );
                                dbDriver.deleteUserContext(
                                    new UserContextDeleteOptions(
                                        userId
                                    )
                                );
                            }
                        );
                        sendSilently(BotMessages.DISH_WAS_DELETED_WITH_SUCCESS, update);
                    } catch(NotFoundDishException e) {
                        sendSilently(BotMessages.THIS_DISH_NAME_IS_NOT_FROM_LIST, update);
                    } catch(Exception e) {
                        sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                        System.out.println("Ошибка : " + e.getMessage());
                    }
                },
                Flag.TEXT,
                isSpecifiedContext(DELETE)
            )
            .build();
    }
}