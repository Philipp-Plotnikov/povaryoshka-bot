package telegram.replies.factory;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import telegram.bot.PovaryoshkaBot;

import java.util.List;

public interface IReplyFactory {
    @NonNull
    List<@NonNull AbilityExtension> getReplyList(@NonNull PovaryoshkaBot povaryoshkaBot);
}
