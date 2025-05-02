package telegram.commands;

import java.sql.SQLException;

import models.commons.RequestContext;
import models.db.sqlops.feedback.FeedbackInsertOptions;

import org.checkerframework.checker.nullness.qual.NonNull;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;

import static models.commands.CommandStates.FEEDBACK_UPDATE;
import static models.commands.MultiStateCommandTypes.*;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;
import org.telegram.telegrambots.meta.api.objects.Update;

import language.ru.BotMessages;
import models.db.sqlops.usercontext.UserContextInsertOptions;

import static models.commands.CommandConfig.FEEDBACK_COMMAND_SETTINGS;
import telegram.bot.PovaryoshkaBot;
import utilities.LoggerUtilities;


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
                LoggerUtilities.fillInLoggerFields(
                        new RequestContext(
                                userId,
                                null,
                                FEEDBACK.getValue(),
                                null
                        )
                );
                logger.info("FeedbackCommand was invoked");
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
                    logger.info("UserContext was inserted in CreateDishCommand");
                } catch(SQLException e) {
                    sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                    logger.error(String.valueOf(e));
                } finally {
                    LoggerUtilities.clearLoggerField();
                }
            })
            .reply((action, update) -> {
                    try {
                        final String feedbackText = update.getMessage().getText().trim();
                        final long userId = update.getMessage().getFrom().getId();
                        LoggerUtilities.fillInLoggerFields(
                                new RequestContext(
                                        userId,
                                        null,
                                        FEEDBACK.getValue(),
                                        null
                                )
                        );
                        dbDriver.executeAsTransaction(
                            () -> {
                                dbDriver.insertFeedback(
                                    new FeedbackInsertOptions(
                                        userId,
                                        feedbackText
                                    )
                                );
                                dbDriver.deleteUserContext(
                                    new UserContextDeleteOptions(
                                    update.getMessage().getFrom().getId()
                                )
                            );
                        }
                    );
                    sendSilently(BotMessages.USER_FEEDBACK_WAS_SAVED, update);
                    logger.info("FeedbackCommand was finished successfully");
                    } catch(Exception e) {
                        sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                        logger.error(e.getMessage());
                    } finally {
                        LoggerUtilities.clearLoggerField();
                    }
                },
                Flag.TEXT,
                isSpecifiedContext(FEEDBACK)
            )
            .build();
    }
}
