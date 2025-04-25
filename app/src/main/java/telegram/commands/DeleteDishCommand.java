package telegram.commands;

import models.db.sqlops.dish.DishDeleteOptions;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import models.db.sqlops.usercontext.UserContextInsertOptions;
import models.exceptions.db.sqlops.NotFoundDishException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;

import static models.commands.CommandStates.DISH_NAME;
import static models.commands.MultiStateCommandTypes.*;
import static models.logger.LoggerFields.*;
import static models.logger.LoggerFields.LOG_DISH_NAME;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

import org.checkerframework.checker.nullness.qual.NonNull;

import static models.commands.CommandConfig.DELETE_DISH_COMMAND_SETTINGS;

import org.telegram.telegrambots.meta.api.objects.Update;

import language.ru.BotMessages;
import telegram.bot.PovaryoshkaBot;

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

                MDC.put(LOG_USER_ID, String.valueOf(userId));
                logger.info("User: {} used DeleteDishCommand", userId);

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

                    MDC.put(LOG_COMMAND_TYPE, DELETE.getValue());
                    MDC.put(LOG_COMMAND_STATE, DISH_NAME.getValue());
                    MDC.put(LOG_DISH_NAME, null);
                    logger.info("User: {} inserted context in DeleteDishCommand", userId);
                    MDC.clear();

                } catch(SQLException e) {
                    sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                    logger.error(e.getMessage());
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

                                MDC.put(LOG_USER_ID, String.valueOf(userId));
                                MDC.put(LOG_COMMAND_TYPE, DELETE.getValue());
                                MDC.put(LOG_COMMAND_STATE, DISH_NAME.getValue());
                                MDC.put(LOG_DISH_NAME, dishName);
                                logger.info("User: {} deleted {} dish", userId, dishName);
                                MDC.clear();

                                dbDriver.deleteUserContext(
                                    new UserContextDeleteOptions(userId)
                                );
                            }
                        );
                        sendSilently(BotMessages.DISH_WAS_DELETED_WITH_SUCCESS, update);

                        MDC.put(LOG_USER_ID, String.valueOf(userId));
                        logger.info("User: {} ended DeleteDishCommand", userId);
                        MDC.clear();

                    } catch(NotFoundDishException e) {
                        sendSilently(BotMessages.THIS_DISH_NAME_IS_NOT_FROM_LIST, update);
                    } catch(Exception e) {
                        sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                        logger.error(e.getMessage());
                    }
                },
                Flag.TEXT,
                isSpecifiedContext(DELETE)
            )
            .build();
    }
}