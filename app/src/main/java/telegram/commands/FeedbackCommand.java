package telegram.commands;

import java.sql.SQLException;

import models.commands.CommandStates;
import models.db.sqlops.feedback.FeedbackInsertOptions;

import org.checkerframework.checker.nullness.qual.NonNull;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;

import static models.commands.CommandStates.DISH_NAME;
import static models.commands.CommandStates.FEEDBACK_UPDATE;
import static models.commands.MultiStateCommandTypes.FEEDBACK;
import static models.commands.MultiStateCommandTypes.GET;
import static models.logger.LoggerFields.*;
import static models.logger.LoggerFields.LOG_DISH_NAME;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;
import org.telegram.telegrambots.meta.api.objects.Update;

import language.ru.BotMessages;
import models.db.sqlops.usercontext.UserContextInsertOptions;

import static models.commands.CommandConfig.FEEDBACK_COMMAND_SETTINGS;
import telegram.bot.PovaryoshkaBot;


final public class FeedbackCommand extends AbstractCommand {
    @NonNull
    private final Logger logger = LoggerFactory.getLogger(FeedbackCommand.class);

    public FeedbackCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
    }

    @NonNull
    public Ability feedback() {
        return Ability.builder()
            .name(FEEDBACK_COMMAND_SETTINGS.commandName())
            .info(FEEDBACK_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL)
            .action(ctx -> {
                final Update update = ctx.update();
                final long userId = ctx.user().getId();

                MDC.put(LOG_USER_ID, String.valueOf(userId));
                logger.info("User: {} used FeedbackCommand", userId);

                try {
                    sendSilently(BotMessages.WRITE_FEEDBACK, update);
                    dbDriver.insertUserContext(
                        new UserContextInsertOptions(
                            userId,
                            FEEDBACK,
                            FEEDBACK_UPDATE,
                            null
                        )
                    );

                    MDC.put(LOG_COMMAND_TYPE, FEEDBACK.getValue());
                    MDC.put(LOG_COMMAND_STATE, FEEDBACK_UPDATE.getValue());
                    MDC.put(LOG_DISH_NAME, null);
                    logger.info("User: {} inserted context in FeedbackCommand", userId);
                    MDC.clear();

                } catch(SQLException e) {
                    sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                    logger.error(e.getMessage());
                }
            })
            .reply((action, update) -> {
                    try {
                        final String feedbackText = update.getMessage().getText().trim();
                        final long userId = update.getMessage().getFrom().getId();
                        dbDriver.executeAsTransaction(
                            () -> {
                                dbDriver.insertFeedback(
                                    new FeedbackInsertOptions(
                                        userId,
                                        feedbackText
                                    )
                                );

                                MDC.put(LOG_USER_ID, String.valueOf(userId));
                                MDC.put(LOG_COMMAND_TYPE, FEEDBACK.getValue());
                                MDC.put(LOG_COMMAND_STATE, FEEDBACK_UPDATE.getValue());
                                MDC.put(LOG_DISH_NAME, null);
                                logger.info("User: {} wrote feedback", userId);
                                MDC.clear();

                                dbDriver.deleteUserContext(
                                    new UserContextDeleteOptions(
                                    update.getMessage().getFrom().getId()
                                )
                            );
                        }
                    );
                    sendSilently(BotMessages.USER_FEEDBACK_WAS_SAVED, update);

                    MDC.put(LOG_USER_ID, String.valueOf(userId));
                    logger.info("User: {} ended FeedbackCommand", userId);
                    MDC.clear();

                    } catch(Exception e) {
                        sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                        logger.error(e.getMessage());
                    }
                },
                Flag.TEXT,
                isSpecifiedContext(FEEDBACK)
            )
            .build();
    }
}
