package telegram.commands;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import models.commands.MultiStateCommandTypes;
import models.commons.SendOptions;
import models.db.sqlops.usercontext.UserContextSelectOptions;
import models.dtos.UserContextDTO;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import dbdrivers.IDbDriver;
import language.ru.BotMessages;
import models.db.sqlops.dish.DishListSelectOptions;
import models.dtos.DishDTO;
import telegram.bot.PovaryoshkaBot;


public abstract class AbstractCommand implements AbilityExtension {
    @NonNull
    protected final PovaryoshkaBot povaryoshkaBot;

    @NonNull
    protected final IDbDriver dbDriver;

    protected AbstractCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
        dbDriver = povaryoshkaBot.getDbDriver();
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

    @NonNull
    protected Predicate<Update> isSpecifiedContext(@NonNull MultiStateCommandTypes commandType) {
        return update -> {
            if (isEndCommand(update)) {
                return false;
            }
            boolean isSpecifiedContext = false;
            try {
                isSpecifiedContext = isSpecifiedMultiStateCommandType(update, commandType);
            } catch (SQLException e) {
                System.out.println(e);
            }
            return isSpecifiedContext;
        };
    }

    private boolean isEndCommand(@NonNull Update update) {
        return update.getMessage().getText().equals("/end");
    }

    private boolean isSpecifiedMultiStateCommandType(@NonNull Update update, @NonNull MultiStateCommandTypes commandType) throws SQLException {
        final UserContextDTO userContextDTO = dbDriver.selectUserContext(
                new UserContextSelectOptions(
                        update.getMessage().getFrom().getId()
                )
        );
        return userContextDTO != null && userContextDTO.getMultiStateCommandTypes() == commandType;
    }
}