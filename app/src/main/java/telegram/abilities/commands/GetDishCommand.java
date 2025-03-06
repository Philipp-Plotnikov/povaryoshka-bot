package telegram.abilities.commands;

import language.ru.BotMessages;
import models.commons.SendOptions;
import models.db.sqlops.dish.DishSelectOptions;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import models.db.sqlops.usercontext.UserContextInsertOptions;
import models.dtos.DishDTO;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.meta.api.objects.Update;
import telegram.bot.PovaryoshkaBot;
import utilities.factory.FormatterFactory;
import utilities.factory.IIngredientsFormatter;

import java.sql.SQLException;
import java.util.List;

import static models.commands.CommandConfig.GET_DISH_COMMAND_SETTINGS;
import static models.commands.CommandStates.DISH_NAME;
import static models.commands.MultiStateCommandTypes.GET;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;


public class GetDishCommand extends AbstractCommand {

    public GetDishCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
    }

    @NonNull
    public Ability getDish() {
        return Ability.builder()
            .name(GET_DISH_COMMAND_SETTINGS.commandName())
            .info(GET_DISH_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL)
            .action(ctx -> {
                final Update update = ctx.update();
                try {
                    final String message = getUserDishList(ctx);
                    if (message == null) {
                        sendSilently(BotMessages.USER_DOES_NOT_HAVE_DISHES, update);
                        return;
                    }
                    sendSilently(message, update);
                    sendSilently(BotMessages.WRITE_DISH_NAME_FROM_LIST_TO_GET, update);
                    dbDriver.insertUserContext(
                            new UserContextInsertOptions(
                                    ctx.user().getId(),
                                    GET,
                                    DISH_NAME,
                                    null
                            )
                    );
                } catch(SQLException e) {
                    sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                    System.out.println("Ошибка при вставке: " + e.getMessage());
                }
            })
            .reply((action, update) -> {
                    try {
                        final long userId = update.getMessage().getFrom().getId();
                        final String dishName = update.getMessage().getText().trim();
                        final DishDTO selectedDish = dbDriver.selectDish(
                            new DishSelectOptions(userId, dishName)
                        );
                        if (selectedDish == null) {
                            sendSilently(BotMessages.THIS_DISH_NAME_IS_NOT_FROM_LIST, update);
                            return;
                        }
                        final String formatDishInfo = getFormatDishInfo(selectedDish);
                        final SendOptions markdown = new SendOptions(true);
                        sendSilently(BotMessages.USER_DISH_IS, update);
                        sendSilently(formatDishInfo, update, markdown);
                        dbDriver.deleteUserContext(
                            new UserContextDeleteOptions(userId)
                        );
                    } catch(Exception e) {
                        sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                        System.out.println("Ошибка : " + e.getMessage());
                    }
                },
                Flag.TEXT,
                isSpecifiedContext(GET)
            )
            .build();
    }

    @NonNull
    private String getFormatDishInfo(@NonNull DishDTO selectedDish) {
        final String formatIngredienListInfo = getFormatIngredientListInfo(selectedDish);
        final String formatRecipeInfo = getFormatRecipeInfo(selectedDish);
        final String formatDishInfo = String.format(
            "%s:\n`%s`\n\n%s:\n`%s`\n\n%s:\n`%s`",
            BotMessages.DISH_NAME,
            selectedDish.getName(),
            BotMessages.INGREDIENTS,
            formatIngredienListInfo,
            BotMessages.RECIPE,
            formatRecipeInfo
        );
        return formatDishInfo;
    }

    @NonNull
    private String getFormatIngredientListInfo(@NonNull DishDTO selectedDish) {
        final List<String> ingredientList = selectedDish.getIngredientList();
        final IIngredientsFormatter ingredientsFormatter = FormatterFactory.getIngredientsFormat();
        return ingredientList != null
            ? ingredientsFormatter.formatOutput(ingredientList)
            : BotMessages.NO_INFO;
    }

    @NonNull
    private String getFormatRecipeInfo(@NonNull DishDTO selectedDish) {
        final String recipe = selectedDish.getRecipe();
        return recipe != null
            ? recipe
            : BotMessages.NO_INFO;
    }
}