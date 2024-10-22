package telegram.commands;

import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import static models.commands.CommandConfig.DELETE_DISH_COMMAND_SETTINGS;
import telegram.bot.PovaryoshkaBot;

public class DeleteDishCommand implements AbilityExtension {
    private final PovaryoshkaBot povaryoshkaBot;

    public DeleteDishCommand(final PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
    }

    public Ability deleteDish() {
        return Ability.builder()
            .name(DELETE_DISH_COMMAND_SETTINGS.commandName())
            .info(DELETE_DISH_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL) // ?
            .action(ctx -> {
                povaryoshkaBot.getSilent().send("delete action", ctx.chatId());
            })
            .build();
    }
}