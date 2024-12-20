package telegram.commands;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import dbdrivers.DbDriver;
import language.ru.BotMessages;
import models.db.sqlops.dish.DishListSelectOptions;
import models.dtos.DishDTO;
import telegram.bot.PovaryoshkaBot;


public abstract class AbstractCommand implements AbilityExtension {
    @NonNull
    protected final PovaryoshkaBot povaryoshkaBot;

    @NonNull
    protected final DbDriver dbDriver;

    protected AbstractCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
        dbDriver = povaryoshkaBot.getDbDriver();
    }

    @NonNull
    protected Optional<Message> sendSilently(@NonNull String message, @NonNull Update update) {
        return povaryoshkaBot.getSilent().send(message, update.getMessage().getChatId());
    }

    @Nullable
    protected String getUserDishList(MessageContext ctx) throws SQLException {
        if (ctx == null) {
            return null;
        }
        final List<DishDTO> dishList = dbDriver.selectDishList(
            new DishListSelectOptions(ctx.user().getId())
        );
        if (dishList == null) {
            return null;
        }
        final StringBuilder messageBuilder = new StringBuilder(
            String.format("%s\n", BotMessages.USER_DISHES_ARE)
        );
        for (DishDTO dish : dishList) {
            messageBuilder.append(String.format("- %s\n", dish.getName()));
        }
        return messageBuilder.toString();
    }
}
