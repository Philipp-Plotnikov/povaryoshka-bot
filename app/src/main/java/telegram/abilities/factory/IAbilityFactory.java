package telegram.abilities.factory;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import telegram.bot.PovaryoshkaBot;

import java.util.List;

public interface IAbilityFactory {
    @NonNull
    List<@NonNull AbilityExtension> getCommandList(@NonNull final PovaryoshkaBot povaryoshkaBot);
}