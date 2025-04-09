package telegram.commands;

import java.sql.SQLException;

import models.commands.CommandStates;
import models.db.sqlops.feedback.FeedbackInsertOptions;

import org.checkerframework.checker.nullness.qual.NonNull;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;

import static models.commands.MultiStateCommandTypes.FEEDBACK;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;
import org.telegram.telegrambots.meta.api.objects.Update;

import language.ru.BotMessages;
import models.db.sqlops.usercontext.UserContextInsertOptions;

import static models.commands.CommandConfig.FEEDBACK_COMMAND_SETTINGS;
import telegram.bot.PovaryoshkaBot;


final public class FeedbackCommand extends AbstractCommand {

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
                try {
                    sendSilently(BotMessages.WRITE_FEEDBACK, update);
                    dbDriver.insertUserContext(
                        new UserContextInsertOptions(
                            ctx.user().getId(),
                            FEEDBACK,
                            CommandStates.FEEDBACK_UPDATE,
                            null
                        )
                    );
                } catch(SQLException e) {
                    sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                    System.out.println("Ошибка при вставке контекста: " + e.getMessage());
                }
            })
            .reply((action, update) -> {
                    try {
                        final String feedbackText = update.getMessage().getText().trim();
                        dbDriver.executeAsTransaction(
                            () -> {
                                dbDriver.insertFeedback(
                                    new FeedbackInsertOptions(
                                        update.getMessage().getFrom().getId(),
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
                    } catch(Exception e) {
                        sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                        System.out.println("Ошибка при обработке отзыва: " + e.getMessage());
                    }
                },
                Flag.TEXT,
                isSpecifiedContext(FEEDBACK)
            )
            .build();
    }
}
