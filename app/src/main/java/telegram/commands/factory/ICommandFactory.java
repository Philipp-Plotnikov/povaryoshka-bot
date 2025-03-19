package telegram.commands.factory;

import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import telegram.bot.PovaryoshkaBot;


public interface ICommandFactory {
    @NonNull
    Map<String, @Nullable AbilityExtension> getCommandMap(@NonNull final PovaryoshkaBot povaryoshkaBot);
}