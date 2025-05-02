package telegram.commands;

import models.commons.RequestContext;
import models.db.sqlops.dish.DishDeleteOptions;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import models.db.sqlops.usercontext.UserContextInsertOptions;
import models.exceptions.db.sqlops.NotFoundDishException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;

import static models.commands.CommandStates.DISH_NAME;
import static models.commands.MultiStateCommandTypes.*;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

import org.checkerframework.checker.nullness.qual.NonNull;

import static models.commands.CommandConfig.DELETE_DISH_COMMAND_SETTINGS;

import org.telegram.telegrambots.meta.api.objects.Update;

import language.ru.BotMessages;
import telegram.bot.PovaryoshkaBot;
import utilities.LoggerUtilities;

import java.sql.SQLException;


final public class DeleteDishCommand extends AbstractCommand {
    @NonNull
    private final Logger logger = LoggerFactory.getLogger(DeleteDishCommand.class);

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
                final long userId = ctx.user().getId();
                LoggerUtilities.fillInLoggerFields(
                        new RequestContext(
                                userId,
                                null,
                                DELETE.getValue(),
                                null
                        )
                );
                logger.info("DeleteDishCommand was invoked");
                try {
                    final String message = getUserDishList(ctx);
                    if (message == null) {
                        sendSilently(BotMessages.USER_DOES_NOT_HAVE_DISHES, update);
                        logger.debug("User doesn't have dishes");
                        return;
                    }
                    sendSilently(message, update);
                    sendSilently(BotMessages.WRITE_DISH_NAME_FROM_LIST_TO_DELETE, update);
                    dbDriver.insertUserContext(
                        new UserContextInsertOptions(
                            userId,
                            DELETE,
                            DISH_NAME,
                            null
                        )
                    );
                    logger.info("UserContext was inserted in DeleteDishCommand");
                } catch(SQLException e) {
                    sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                    logger.error(String.valueOf(e));
                } finally {
                    LoggerUtilities.clearLoggerField();
                }
            })
            .reply((action, update) -> {
                    try {
                        final long userId = update.getMessage().getFrom().getId();
                        final String dishName = update.getMessage().getText().trim();
                        LoggerUtilities.fillInLoggerFields(
                                new RequestContext(
                                        userId,
                                        dishName,
                                        DELETE.getValue(),
                                        null
                                )
                        );
                        dbDriver.executeAsTransaction(
                            () -> {
                                dbDriver.deleteDish(
                                    new DishDeleteOptions(
                                        userId,
                                        dishName
                                    )
                                );
                                dbDriver.deleteUserContext(
                                    new UserContextDeleteOptions(userId)
                                );
                            }
                        );
                        sendSilently(BotMessages.DISH_WAS_DELETED_WITH_SUCCESS, update);
                        logger.info("Dish was deleted successfully");
                    } catch(NotFoundDishException e) {
                        sendSilently(BotMessages.THIS_DISH_NAME_IS_NOT_FROM_LIST, update);
                        logger.debug("The chosen dish wasn't in the user list");
                    } catch(Exception e) {
                        sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                        logger.error(String.valueOf(e));
                    } finally {
                        LoggerUtilities.clearLoggerField();
                    }
                },
                Flag.TEXT,
                isSpecifiedContext(DELETE)
            )
            .build();
    }
}