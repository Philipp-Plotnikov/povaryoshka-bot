package telegram.commands.factory;

import java.util.List;

import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import telegram.bot.PovaryoshkaBot;

public interface CommandFactory {
    List<AbilityExtension> getCommandList(final PovaryoshkaBot povaryoshkaBot);
}