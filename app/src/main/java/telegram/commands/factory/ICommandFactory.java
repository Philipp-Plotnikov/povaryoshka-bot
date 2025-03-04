package telegram.commands.factory;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import telegram.bot.PovaryoshkaBot;

public interface ICommandFactory {
    @NonNull
    List<@NonNull AbilityExtension> getCommandList(@NonNull final PovaryoshkaBot povaryoshkaBot);
}