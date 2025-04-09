package telegram.replies.factory;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import telegram.bot.PovaryoshkaBot;

import java.util.Map;


public interface IReplyFactory {
    @NonNull
    Map<String, @Nullable AbilityExtension> createReplyMap(@NonNull final PovaryoshkaBot povaryoshkaBot);
}