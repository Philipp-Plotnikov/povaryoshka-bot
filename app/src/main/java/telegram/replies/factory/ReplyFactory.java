package telegram.replies.factory;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import telegram.bot.PovaryoshkaBot;
import telegram.replies.DefaultReply;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ReplyFactory implements IReplyFactory {
    @Override
    @NonNull
    public List<@NonNull AbilityExtension> createReplyList(@NonNull PovaryoshkaBot povaryoshkaBot) {
        final ArrayList<AbilityExtension> replyList = new ArrayList<>();
        replyList.add(new DefaultReply(povaryoshkaBot));
        return Collections.unmodifiableList(replyList);
    }
}