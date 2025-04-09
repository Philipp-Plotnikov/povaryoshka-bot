package telegram.replies;

import models.commons.SendOptions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import telegram.bot.PovaryoshkaBot;

import java.util.Optional;


public abstract class AbstractReply implements AbilityExtension {
    @NonNull
    protected final PovaryoshkaBot povaryoshkaBot;

    public AbstractReply(@NonNull PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
    }

    @NonNull
    protected Optional<Message> sendSilently(@NonNull String message, @NonNull Update update) {
        return sendSilently(message, update, new SendOptions(false));
    }

    @NonNull
    protected Optional<Message> sendSilently(@NonNull String message, @NonNull Update update, @NonNull SendOptions sendOptions) {
        return (sendOptions.isMarkdown())
                ? povaryoshkaBot.getSilent().sendMd(message, update.getMessage().getChatId())
                : povaryoshkaBot.getSilent().send(message, update.getMessage().getChatId());
    }
}