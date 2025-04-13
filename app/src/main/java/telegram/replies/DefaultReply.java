package telegram.replies;

import language.ru.BotMessages;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import static models.replies.ReplyConfig.DEFAULT_REPLY_SETTINGS;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

import org.telegram.telegrambots.meta.api.objects.Update;
import telegram.bot.PovaryoshkaBot;


final public class DefaultReply extends AbstractReply {
    public DefaultReply(@NonNull PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
    }

    @NonNull
    public Ability defaultReply() {
        return Ability.builder()
                .name(DEFAULT_REPLY_SETTINGS.replyName())
                .info(DEFAULT_REPLY_SETTINGS.replyDescription())
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    final Update update = ctx.update();
                    sendSilently(BotMessages.DEFAULT_REPLY_MESSAGE, update);
                })
                .build();
    }
}