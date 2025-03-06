package telegram.abilities.replies;

import org.checkerframework.checker.nullness.qual.NonNull;
import telegram.abilities.factory.AbstractAbility;
import telegram.bot.PovaryoshkaBot;

public class AbstractReply extends AbstractAbility {

    protected AbstractReply(@NonNull PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
    }
}