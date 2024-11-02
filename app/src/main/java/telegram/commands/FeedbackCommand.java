package telegram.commands;

import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;

import static models.commands.CommandConfig.FEEDBACK_COMMAND_SETTINGS;
import telegram.bot.PovaryoshkaBot;

public class FeedbackCommand implements AbilityExtension {
    @NonNull
    private final PovaryoshkaBot povaryoshkaBot;

    private boolean isInFeedbackContext = false;

    public FeedbackCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
    }

    @NonNull
    public Ability feedback() {
        return Ability.builder()
            .name(FEEDBACK_COMMAND_SETTINGS.commandName())
            .info(FEEDBACK_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL) // ?
            .action(ctx -> {
                isInFeedbackContext = true;
                povaryoshkaBot.getSilent().send("feedback action", ctx.chatId());
            })
            .reply((action, update) -> {
                    povaryoshkaBot.getSilent().send("feedback reply", update.getMessage().getChatId());
                },
                isInFeedbackContext()
            )
            .build();
    }

    private Predicate<Update> isInFeedbackContext() {
        return update -> isInFeedbackContext;
    }
}