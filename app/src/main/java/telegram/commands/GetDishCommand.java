package telegram.commands;

import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import static models.commands.CommandConfig.GET_DISH_COMMAND_SETTINGS;
import telegram.bot.PovaryoshkaBot;

public class GetDishCommand implements AbilityExtension {
    @NonNull
    private final PovaryoshkaBot povaryoshkaBot;

    public GetDishCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
    }

    @NonNull
    public Ability getDish() {
        return Ability.builder()
            .name(GET_DISH_COMMAND_SETTINGS.commandName())
            .info(GET_DISH_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL) // ?
            .action(ctx -> {
                povaryoshkaBot.getSilent().send("get action", ctx.chatId());
            })
            .build();
    }
}