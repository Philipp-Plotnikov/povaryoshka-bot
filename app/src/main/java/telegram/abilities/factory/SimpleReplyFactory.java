package telegram.abilities.factory;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import telegram.abilities.replies.UnknownReply;
import telegram.bot.PovaryoshkaBot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleReplyFactory implements IAbilityFactory {
    @Override
    @NonNull
    public List<@NonNull AbilityExtension> getCommandList(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        final ArrayList<AbilityExtension> simpleCommandList = new ArrayList<>();
        simpleCommandList.add(new UnknownReply(povaryoshkaBot));
        return Collections.unmodifiableList(simpleCommandList);
    }
}