package telegram.abilities.replies;

import language.ru.BotMessages;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import telegram.bot.PovaryoshkaBot;

public class UnknownReply extends AbstractReply {

    public UnknownReply(@NonNull PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
    }

    @NonNull
    public Reply unknownMessage(){
        return Reply.of(
                (PovaryoshkaBot, update) -> {
                    sendSilently(BotMessages.UNKNOWN_COMMAND, update);
                },
                isInCommandContext(), Flag.MESSAGE);
    }
}