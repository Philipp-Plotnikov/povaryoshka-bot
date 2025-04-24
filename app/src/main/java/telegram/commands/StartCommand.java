package telegram.commands;

import language.ru.BotMessages;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.objects.Update;
import telegram.bot.PovaryoshkaBot;

import static models.commands.CommandConfig.START_COMMAND_SETTINGS;

import static models.logger.LoggerFields.LOG_USER_ID;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;


final public class StartCommand extends AbstractCommand {
    @NonNull
    private final Logger logger = LoggerFactory.getLogger(StartCommand.class);

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
                    final long userId = ctx.user().getId();
                    sendSilently(BotMessages.START_OUTPUT, update);

                    MDC.put(LOG_USER_ID, String.valueOf(userId));
                    logger.info("User: {} used StartCommand", userId);
                    MDC.clear();
                })
                .build();
    }
}
