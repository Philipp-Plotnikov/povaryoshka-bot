package telegram.commands;

import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import static models.commands.CommandConfig.CREATE_DISH_COMMAND_SETTINGS;
import telegram.bot.PovaryoshkaBot;

public class CreateDishCommand implements AbilityExtension {
    private final PovaryoshkaBot povaryoshkaBot;

    public CreateDishCommand(final PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
    }

    public Ability createDish() {
        return Ability.builder()
            .name(CREATE_DISH_COMMAND_SETTINGS.commandName())
            .info(CREATE_DISH_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL) // ?
            .action(ctx -> {
                povaryoshkaBot.getSilent().send("create action", ctx.chatId());
            })
            .build();
    }
}