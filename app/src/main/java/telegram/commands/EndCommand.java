package telegram.commands;

import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import static models.commands.CommandConfig.END_COMMAND_SETTINGS;
import telegram.bot.PovaryoshkaBot;

public class EndCommand implements AbilityExtension {
    private final PovaryoshkaBot povaryoshkaBot;

    public EndCommand(final PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
    }

    public Ability end() {
        return Ability.builder()
            .name(END_COMMAND_SETTINGS.commandName())
            .info(END_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL) // ?
            .action(ctx -> {

                povaryoshkaBot.getSilent().send("delete action", ctx.chatId());
            })
            .build();
    }
}