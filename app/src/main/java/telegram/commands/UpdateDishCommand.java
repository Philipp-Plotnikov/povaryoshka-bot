package telegram.commands;

import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import static models.commands.CommandConfig.UPDATE_DISH_COMMAND_SETTINGS;
import telegram.bot.PovaryoshkaBot;

public class UpdateDishCommand implements AbilityExtension {
    @NonNull
    private final PovaryoshkaBot povaryoshkaBot;

    public UpdateDishCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
    }

    @NonNull
    public Ability updateDish() {
        return Ability.builder()
            .name(UPDATE_DISH_COMMAND_SETTINGS.commandName())
            .info(UPDATE_DISH_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL) // ?
            .action(ctx -> {
                povaryoshkaBot.getSilent().send("update action", ctx.chatId());
            })
            .build();
    }
}