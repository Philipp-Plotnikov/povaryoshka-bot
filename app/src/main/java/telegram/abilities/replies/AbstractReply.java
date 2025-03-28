package telegram.abilities.replies;

import models.commands.CommandMap;
import models.db.sqlops.usercontext.UserContextSelectOptions;
import models.dtos.UserContextDTO;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.meta.api.objects.Update;
import telegram.abilities.factory.AbstractAbility;
import telegram.bot.PovaryoshkaBot;

import java.sql.SQLException;
import java.util.function.Predicate;

public class AbstractReply extends AbstractAbility {

    protected AbstractReply(@NonNull PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
    }

    @NonNull
    protected Predicate<Update> isInCommandContext() {
        return update -> {
            boolean isInCommandContext = false;
            try {
                isInCommandContext = isInAnyCommandContext(update);
            } catch (SQLException e) {
                System.out.println(e);
            }
            return isInCommandContext;
        };
    }

    private boolean isInAnyCommandContext(@NonNull Update update) throws SQLException {
        final UserContextDTO userContextDTO = dbDriver.selectUserContext(
                new UserContextSelectOptions(
                        update.getMessage().getFrom().getId()
                )
        );
        final String msg = update.getMessage().getText().trim();
        return userContextDTO == null && !CommandMap.getInstance().containsCommand(msg);
    }
}