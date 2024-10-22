package telegram.commands;

import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import static models.commands.CommandConfig.FEEDBACK_COMMAND_SETTINGS;
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
                povaryoshkaBot.getSilent().send("feedback action", ctx.chatId());
            })
            .build();
    }
}