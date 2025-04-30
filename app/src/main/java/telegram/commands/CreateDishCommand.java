package telegram.commands;

import java.sql.SQLException;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import models.commons.RequestContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;

import static models.commands.CommandStates.*;
import static models.commands.MultiStateCommandTypes.*;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

import org.telegram.telegrambots.meta.api.objects.Update;

import language.ru.BotMessages;

import static models.commands.CommandConfig.CREATE_DISH_COMMAND_SETTINGS;

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
import utilities.LoggerUtilities;
import utilities.factory.FormatterFactory;
import utilities.factory.IIngredientsFormatter;


final public class CreateDishCommand extends AbstractCommand {
    @NonNull
    private final EnumMap<@NonNull CommandStates, ICommandStateHandler> stateHandlersMap = new EnumMap<>(CommandStates.class);

    @NonNull
    private final Logger logger = LoggerFactory.getLogger(CreateDishCommand.class);

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
                    final long userId = ctx.user().getId();
                    LoggerUtilities.fillInLoggerFields(
                            new RequestContext(
                                    userId,
                                    null,
                                    CREATE.getValue(),
                                    null
                            )
                    );
                    logger.info("CreateDishCommand was invoked", userId);
                    try {
                        sendSilently(BotMessages.WRITE_DISH_NAME, update);
                        dbDriver.insertUserContext(
                            new UserContextInsertOptions(
                                userId,
                                CREATE,
                                DISH_NAME_UPDATE,
                                null
                            )
                        );
                        logger.info("UserContext was inserted in CreateDishCommand", userId);
                    } catch (SQLException e) {
                        sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                        logger.error(String.valueOf(e));
                    } finally {
                        LoggerUtilities.clearLoggerField();
                    }
                })
                .reply((action, update) -> {
                            try {
                                long userId = update.getMessage().getFrom().getId();
                                final UserContextDTO userContextDTO = dbDriver.selectUserContext(
                                    new UserContextSelectOptions(userId)
                                );
                                if (userContextDTO != null) {
                                    LoggerUtilities.fillInLoggerFields(
                                            new RequestContext(
                                                    userId,
                                                    userContextDTO.getDishName(),
                                                    userContextDTO.getMultiStateCommandTypes().getValue(),
                                                    userContextDTO.getCommandState().getValue()
                                            )
                                    );
                                    final CommandStates commandState = userContextDTO.getCommandState();
                                    final ICommandStateHandler commandStateHandler = stateHandlersMap.get(commandState);
                                    if (commandStateHandler == null) {
                                        throw new Exception("Dont have needed handlers");
                                    }
                                    commandStateHandler.handle(update, userContextDTO);
                                }
                            } catch (Exception e) {
                                sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                                logger.error(String.valueOf(e));
                            } finally {
                                LoggerUtilities.clearLoggerField();
                            }
                        },
                        Flag.TEXT,
                        isSpecifiedContext(CREATE)
                )
                .build();
    }

    public void handleDishNameUpdateState(
        @NonNull final Update update,
        @NonNull final UserContextDTO userContextDTO
    ) {
        final long userId = update.getMessage().getFrom().getId();
        final String dishName = update.getMessage().getText().trim();
        try {
            dbDriver.executeAsTransaction(
                    () -> {
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
            logger.info("Dish name was chosen");
            sendSilently(BotMessages.WRITE_INGREDIENTS, update);
        } catch (Exception e) {
            sendSilently(BotMessages.DISH_ALREADY_EXISTS, update);
            logger.error(String.valueOf(e));
        }
    }

    public void handleIngredientsUpdateState(
        @NonNull final Update update,
        @NonNull final UserContextDTO userContextDTO
    ) {
        try {
            final String ingredients = update.getMessage().getText().trim();
            final IIngredientsFormatter ingredientsFormatter = FormatterFactory.createIngredientsFormat();
            final List<String> ingredientList = Collections.unmodifiableList(ingredientsFormatter.formatInput(ingredients));
            final long userId = update.getMessage().getFrom().getId();
            final String userDishName = userContextDTO.getDishName();
            dbDriver.executeAsTransaction(
                    () -> {
                        if (userDishName == null) {
                            throw new Exception("in handleIngredientsUpdateState userDishName is null");
                        }
                        dbDriver.updateDish(
                            new DishUpdateOptions(
                                userId,
                                userDishName,
                                null,
                                ingredientList,
                                null
                            )
                        );
                        dbDriver.updateUserContext(
                            new UserContextUpdateOptions(
                                userId,
                                RECIPE_UPDATE,
                                userDishName
                            )
                        );
                    }
            );
            logger.info("Ingredients were added");
            sendSilently(BotMessages.WRITE_RECIPE, update);
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            logger.error(String.valueOf(e));
        }
    }

    public void handleRecipeUpdateState(
        @NonNull final Update update,
        @NonNull final UserContextDTO userContextDTO
    ) {
        final long userId = update.getMessage().getFrom().getId();
        final String dishName = userContextDTO.getDishName();
        try {
            final DishDTO dishDTO = dbDriver.selectDish(
                new DishSelectOptions(userId, dishName)
            );
            dbDriver.executeAsTransaction(
                    () -> {
                        dbDriver.updateDish(
                                new DishUpdateOptions(
                                userId,
                                dishName,
                                null,
                                dishDTO.getIngredientList(),
                                update.getMessage().getText().trim()
                            )
                        );
                        logger.info("Recipe was added");
                        dbDriver.deleteUserContext(
                            new UserContextDeleteOptions(userId)
                        );
                    }
            );
            sendSilently(BotMessages.DISH_WAS_CREATED_WITH_SUCCESS, update);
            logger.info("New dish was created successfully");
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            logger.error(String.valueOf(e));
        }
    }
}