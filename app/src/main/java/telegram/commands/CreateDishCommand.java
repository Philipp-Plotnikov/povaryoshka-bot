package telegram.commands;

import java.sql.SQLException;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

import org.telegram.telegrambots.meta.api.objects.Update;

import language.ru.BotMessages;

import static models.commands.CommandConfig.CREATE_DISH_COMMAND_SETTINGS;
import static models.commands.CommandStates.DISH_NAME_UPDATE;
import static models.commands.CommandStates.INGREDIENTS_UPDATE;
import static models.commands.CommandStates.RECIPE_UPDATE;
import static models.commands.MultiStateCommandTypes.CREATE;

import models.commands.ICommandStateHandler;
import models.commands.CommandStates;
import models.db.sqlops.dish.DishInsertOptions;
import models.db.sqlops.dish.DishSelectOptions;
import models.db.sqlops.dish.DishUpdateOptions;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import models.db.sqlops.usercontext.UserContextInsertOptions;
import models.db.sqlops.usercontext.UserContextSelectOptions;
import models.db.sqlops.usercontext.UserContextUpdateOptions;
import models.dtos.DishDTO;
import models.dtos.UserContextDTO;
import telegram.bot.PovaryoshkaBot;
import utilities.factory.FormatterFactory;
import utilities.factory.IIngredientsFormatter;


public class CreateDishCommand extends AbstractCommand {
    @NonNull
    private final EnumMap<@NonNull CommandStates, ICommandStateHandler> stateHandlersMap = new EnumMap<>(CommandStates.class);

    public CreateDishCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
        initStateHandlersMap();
    }

    private void initStateHandlersMap() {
        stateHandlersMap.put(DISH_NAME_UPDATE, this::handleDishNameUpdateState);
        stateHandlersMap.put(INGREDIENTS_UPDATE, this::handleIngredientsUpdateState);
        stateHandlersMap.put(RECIPE_UPDATE, this::handleRecipeUpdateState);
    }

    @NonNull
    public Ability createDish() {
        return Ability.builder()
                .name(CREATE_DISH_COMMAND_SETTINGS.commandName())
                .info(CREATE_DISH_COMMAND_SETTINGS.commandDescription())
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    final Update update = ctx.update();
                    try {
                        sendSilently(BotMessages.WRITE_DISH_NAME, update);
                        dbDriver.insertUserContext(
                                new UserContextInsertOptions(
                                        ctx.user().getId(),
                                        CREATE,
                                        DISH_NAME_UPDATE,
                                        null
                                )
                        );
                    } catch (SQLException e) {
                        sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                    }
                })
                .reply((action, update) -> {
                            try {
                                final UserContextDTO userContextDTO = dbDriver.selectUserContext(
                                        new UserContextSelectOptions(
                                                update.getMessage().getFrom().getId()
                                        )
                                );
                                if (userContextDTO != null) {
                                    final CommandStates commandState = userContextDTO.getCommandState();
                                    final ICommandStateHandler commandStateHandler = stateHandlersMap.get(commandState);
                                    if (commandStateHandler == null) {
                                        throw new Exception("Dont have needed handlers");
                                    }
                                    commandStateHandler.handle(update, userContextDTO);
                                }
                            } catch (Exception e) {
                                sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                            }
                        },
                        Flag.TEXT,
                        isSpecifiedContext(CREATE)
                )
                .build();
    }

    private void handleDishNameUpdateState(
            @NonNull final Update update,
            @NonNull final UserContextDTO userContextDTO
    ) {
        try {
            dbDriver.executeAsTransaction(
                    () -> {
                        final long userId = update.getMessage().getFrom().getId();
                        final String dishName = update.getMessage().getText().trim();
                        dbDriver.insertDish(
                                new DishInsertOptions(
                                        userId,
                                        dishName,
                                        null,
                                        null
                                )
                        );
                        dbDriver.updateUserContext(
                                new UserContextUpdateOptions(
                                        userId,
                                        INGREDIENTS_UPDATE,
                                        dishName
                                )
                        );
                    }
            );
            sendSilently(BotMessages.WRITE_INGREDIENTS, update);
        } catch (Exception e) {
            sendSilently(BotMessages.DISH_ALREADY_EXISTS, update);
            System.out.println(e);
        }
    }

    private void handleIngredientsUpdateState(
            @NonNull final Update update,
            @NonNull final UserContextDTO userContextDTO
    ) {
        try {
            final String ingredients = update.getMessage().getText().trim();
            final IIngredientsFormatter ingredientsFormatter = FormatterFactory.getIngredientsFormat();
            final List<String> ingredientList = Collections.unmodifiableList(ingredientsFormatter.formatInput(ingredients));
            dbDriver.executeAsTransaction(
                    () -> {
                        final long userId = update.getMessage().getFrom().getId();
                        dbDriver.updateDish(
                                new DishUpdateOptions(
                                        userId,
                                        userContextDTO.getDishName(),
                                        null,
                                        ingredientList,
                                        null
                                )
                        );
                        dbDriver.updateUserContext(
                                new UserContextUpdateOptions(
                                        userId,
                                        RECIPE_UPDATE,
                                        userContextDTO.getDishName()
                                )
                        );
                    }
            );
            sendSilently(BotMessages.WRITE_RECIPE, update);
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            System.out.println(e);
        }
    }

    private void handleRecipeUpdateState(
            @NonNull final Update update,
            @NonNull final UserContextDTO userContextDTO
    ) {
        try {
            final long userId = update.getMessage().getFrom().getId();
            final DishDTO dishDTO = dbDriver.selectDish(
                    new DishSelectOptions(userId, userContextDTO.getDishName())
            );
            dbDriver.executeAsTransaction(
                    () -> {
                        dbDriver.updateDish(
                                new DishUpdateOptions(
                                        userId,
                                        userContextDTO.getDishName(),
                                        null,
                                        dishDTO.getIngredientList(),
                                        update.getMessage().getText().trim()
                                )
                        );
                        dbDriver.deleteUserContext(
                                new UserContextDeleteOptions(userId)
                        );
                    }
            );
            sendSilently(BotMessages.DISH_WAS_CREATED_WITH_SUCCESS, update);
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            System.out.println(e);
        }
    }
}