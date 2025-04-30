package telegram.commands;

import models.commons.RequestContext;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.objects.Update;

import language.ru.BotMessages;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

import org.checkerframework.checker.nullness.qual.NonNull;

import static models.commands.CommandConfig.END_COMMAND_SETTINGS;
import telegram.bot.PovaryoshkaBot;
import utilities.LoggerUtilities;


final public class EndCommand extends AbstractCommand {
    @NonNull
    private final Logger logger = LoggerFactory.getLogger(EndCommand.class);

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
                final long userId = ctx.user().getId();
                LoggerUtilities.fillInLoggerFields(
                        new RequestContext(
                                userId,
                                null,
                                null,
                                null
                        )
                );
                sendSilently(BotMessages.COMMAND_WAS_TERMINATED, update);
                try {
                    dbDriver.deleteUserContext(
                        new UserContextDeleteOptions(userId)
                    );
                logger.info("EndCommand was invoked");
                } catch (Exception e) {
                    sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                    logger.error(String.valueOf(e));
                } finally {
                    LoggerUtilities.clearLoggerField();
                }
            })
            .build();
    }
}