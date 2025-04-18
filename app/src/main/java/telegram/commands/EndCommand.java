package telegram.commands;

import models.db.sqlops.usercontext.UserContextDeleteOptions;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.objects.Update;

import language.ru.BotMessages;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

import org.checkerframework.checker.nullness.qual.NonNull;

import static models.commands.CommandConfig.END_COMMAND_SETTINGS;
import telegram.bot.PovaryoshkaBot;


final public class EndCommand extends AbstractCommand {

    public EndCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
    }

    @NonNull
    public Ability end() {
        return Ability.builder()
            .name(END_COMMAND_SETTINGS.commandName())
            .info(END_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL)
            .action(ctx -> {
                final Update update = ctx.update();
                sendSilently(BotMessages.COMMAND_WAS_TERMINATED, update);
                try {
                    dbDriver.deleteUserContext(
                        new UserContextDeleteOptions(
                            ctx.user().getId()
                        )
                    );
                } catch (Exception e) {
                    sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                    System.out.println("error");;
                };
            })
            .build();
    }
}