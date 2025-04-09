package telegram.replies.factory;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import telegram.bot.PovaryoshkaBot;
import telegram.replies.DefaultReply;

import java.util.*;

import static models.replies.ReplyConfig.DEFAULT_REPLY_SETTINGS;


public class ReplyFactory implements IReplyFactory {
    @Override
    @NonNull
    public Map<String, @NonNull AbilityExtension> createReplyMap(@NonNull PovaryoshkaBot povaryoshkaBot) {
        final Map<String, AbilityExtension> replyMap = new HashMap<>();
        replyMap.put(
                DEFAULT_REPLY_SETTINGS.replyName(),
                new DefaultReply(povaryoshkaBot)
        );
        return Collections.unmodifiableMap(replyMap);
    }
}