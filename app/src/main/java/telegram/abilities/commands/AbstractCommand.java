package telegram.abilities.commands;

import language.ru.BotMessages;
import models.db.sqlops.dish.DishListSelectOptions;
import models.dtos.DishDTO;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import telegram.abilities.factory.AbstractAbility;
import telegram.bot.PovaryoshkaBot;

import java.sql.SQLException;
import java.util.List;


public abstract class AbstractCommand extends AbstractAbility {

    protected AbstractCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
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