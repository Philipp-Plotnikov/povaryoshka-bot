package telegram.commands;

import java.util.function.Predicate;
import java.sql.SQLException;

import models.commands.CommandStates;
import models.db.sqlops.feedback.FeedbackInsertOptions;

import org.checkerframework.checker.nullness.qual.NonNull;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import models.db.sqlops.usercontext.UserContextSelectOptions;
import models.dtos.UserContextDTO;
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


public class FeedbackCommand extends AbstractCommand {

    public FeedbackCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
    }

    @NonNull
    public Ability feedback() {
        return Ability.builder()
            .name(FEEDBACK_COMMAND_SETTINGS.commandName())
            .info(FEEDBACK_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL) // ?
            .action(ctx -> {
                final Update update = ctx.update();
                try {
                    sendSilently(BotMessages.WRITE_FEEDBACK, update);
                    dbDriver.insertUserContext(
                        new UserContextInsertOptions(
                            ctx.user().getId(),
                            FEEDBACK,
                            CommandStates.FEEDBACK,
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
                        final String feedbackText = update.getMessage().getText();
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
                    } catch(Exception e) {
                        sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                        System.out.println("Ошибка при обработке отзыва: " + e.getMessage());
                    }
                },
                Flag.TEXT,
                isFeedbackContext()
            )
            .build();
    }

    private Predicate<Update> isFeedbackContext(){
        return update -> {
            boolean isFeedbackContext = false;
            if (update.getMessage().getText().equals("/end")){
                return false;
            }
            try {
                final UserContextDTO userContextDTO = dbDriver.selectUserContext(
                    new UserContextSelectOptions(
                        update.getMessage().getFrom().getId()
                    )
                );
                if (userContextDTO != null && userContextDTO.getMultiStateCommandTypes() == FEEDBACK) {
                    isFeedbackContext = true;
                }
            } catch(SQLException e) {
                System.out.println(e);
            }
            return isFeedbackContext;
        };
    }
}
