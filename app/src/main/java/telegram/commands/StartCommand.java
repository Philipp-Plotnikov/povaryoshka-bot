package telegram.commands;

import language.ru.BotMessages;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.objects.Update;
import telegram.bot.PovaryoshkaBot;

import static models.commands.CommandConfig.START_COMMAND_SETTINGS;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;


public class StartCommand extends AbstractCommand {
    public StartCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
    }

    public Ability start(){
        return Ability.builder()
                .name(START_COMMAND_SETTINGS.commandName())
                .info(START_COMMAND_SETTINGS.commandDescription())
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    final Update update = ctx.update();
                    sendSilently(BotMessages.START_OUTPUT, update);
                })
                .build();
    }
}
