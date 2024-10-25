package telegram.commands;

import java.sql.SQLException;
import java.util.function.Predicate;

import models.commands.CommandStates;
import models.db.sqlops.feedback.FeedbackInsertOptions;

import models.db.sqlops.usercontext.UserContextDeleteOptions;
import models.db.sqlops.usercontext.UserContextSelectOptions;
import models.dtos.UserContextDTO;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import static models.commands.MultiStateCommandTypes.FEEDBACK;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import models.db.sqlops.usercontext.UserContextInsertOptions;

import static models.commands.CommandConfig.FEEDBACK_COMMAND_SETTINGS;

import org.telegram.telegrambots.meta.api.objects.Update;
import telegram.bot.PovaryoshkaBot;


public class FeedbackCommand implements AbilityExtension {
    private final PovaryoshkaBot povaryoshkaBot;

    public FeedbackCommand(final PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
    }

    public Ability feedback() {
        return Ability.builder()
            .name(FEEDBACK_COMMAND_SETTINGS.commandName())
            .info(FEEDBACK_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL) // ?
                .action(ctx -> {

                    try {
                        povaryoshkaBot.getDbDriver().insertUserContext(
                                new UserContextInsertOptions(
                                        ctx.user().getId(),
                                        FEEDBACK,
                                        CommandStates.FEEDBACK
                                )
                        );
                        povaryoshkaBot.getSilent().send("В следующем сообщении напишите ваш отзыв о нашем сервисе, и мы его сохраним.", ctx.chatId());

                    } catch(SQLException e) {
                        povaryoshkaBot.getSilent().send("Извините, произошла ошибка. Попробуйте позже.", ctx.chatId());
                        System.out.println("Ошибка при вставке контекста: " + e.getMessage());
                    }
                })
                .reply((action, update) -> {
                    try {
                        final UserContextDTO userContextDTO = povaryoshkaBot.getDbDriver().selectUserContext(
                                new UserContextSelectOptions(
                                        update.getMessage().getFrom().getId()
                                )
                        );
                        if (userContextDTO.getMultiStateCommandTypes() == FEEDBACK) {
                            String feedbackText = update.getMessage().getText();
                            povaryoshkaBot.getDbDriver().insertFeedback(
                                    new FeedbackInsertOptions(
                                            update.getMessage().getFrom().getId(),
                                            feedbackText
                                    )
                            );
                        }
                        povaryoshkaBot.getDbDriver().deleteUserContext(
                                new UserContextDeleteOptions(
                                        update.getMessage().getFrom().getId()
                                )
                        );
                    } catch(Exception e) {
                        System.out.println("Ошибка при обработке отзыва: " + e.getMessage());
                    }

                },
                        isFeedbackContext()
                        )
                .build();

    }

    private Predicate<Update> isFeedbackContext(){
        return update -> {
            boolean isFeedbackContext = false;
            try {
                final UserContextDTO userContextDTO = povaryoshkaBot.getDbDriver().selectUserContext(
                        new UserContextSelectOptions(
                                update.getMessage().getFrom().getId()
                        )
                );
                if (userContextDTO.getMultiStateCommandTypes() == FEEDBACK) {
                    isFeedbackContext = true;
                }
            } catch(SQLException e) {
                System.out.println(e);
            }
            return isFeedbackContext;

        };
    }
}