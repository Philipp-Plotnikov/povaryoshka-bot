package telegram.replies.factory;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import telegram.bot.PovaryoshkaBot;
import telegram.replies.DefaultReply;

import java.util.*;

import static models.replies.ReplyConfig.DEFAULT_REPLY_SETTINGS;


public class ReplyFactory implements IReplyFactory {
    @NonNull
    private final static Logger logger = LoggerFactory.getLogger(ReplyFactory.class);

    @Override
    @NonNull
    public Map<String, @NonNull AbilityExtension> createReplyMap(@NonNull PovaryoshkaBot povaryoshkaBot) {
        final Map<String, AbilityExtension> replyMap = new HashMap<>();
        replyMap.put(
                DEFAULT_REPLY_SETTINGS.replyName(),
                new DefaultReply(povaryoshkaBot)
        );
        logger.debug("Created ReplyMap");
        return Collections.unmodifiableMap(replyMap);
    }
}