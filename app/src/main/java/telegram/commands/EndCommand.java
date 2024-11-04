package telegram.commands;

import models.db.sqlops.usercontext.UserContextDeleteOptions;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import static models.commands.CommandConfig.END_COMMAND_SETTINGS;
import telegram.bot.PovaryoshkaBot;

public class EndCommand implements AbilityExtension {
    @NonNull
    private final PovaryoshkaBot povaryoshkaBot;

    public EndCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
    }

    @NonNull
    public Ability end() {
        return Ability.builder()
            .name(END_COMMAND_SETTINGS.commandName())
            .info(END_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL) // ?
            .action(ctx -> {
                povaryoshkaBot.getSilent().send(" Создание блюда завершено.", ctx.chatId());
                try {
                    povaryoshkaBot.getDbDriver().deleteUserContext(
                            new UserContextDeleteOptions(
                                    ctx.user().getId()
                            )
                    );
                } catch (Exception e) {
                    System.out.println("error");;
                };
            })
            .build();
    }
}