package telegram.commands;

import java.sql.SQLException;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;

import static models.commands.CommandStates.*;
import static models.commands.MultiStateCommandTypes.*;
import static models.logger.LoggerFields.*;
import static models.logger.LoggerFields.LOG_DISH_NAME;
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

                    MDC.put(LOG_USER_ID, String.valueOf(userId));
                    logger.info("User: {} used CreateDishCommand", userId);

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

                        MDC.put(LOG_COMMAND_TYPE, CREATE.getValue());
                        MDC.put(LOG_COMMAND_STATE, DISH_NAME_UPDATE.getValue());
                        MDC.put(LOG_DISH_NAME, null);
                        logger.info("User: {} inserted context in CreateDishCommand", userId);
                        MDC.clear();

                    } catch (SQLException e) {
                        sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                        logger.error(e.getMessage());
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
                                logger.error(e.getMessage());
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

                        MDC.put(LOG_USER_ID, String.valueOf(userId));
                        MDC.put(LOG_COMMAND_TYPE, CREATE.getValue());
                        MDC.put(LOG_COMMAND_STATE, DISH_NAME_UPDATE.getValue());
                        MDC.put(LOG_DISH_NAME, dishName);
                        logger.info("User: {} created {} dish", userId, dishName);
                        MDC.clear();

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

            MDC.put(LOG_USER_ID, String.valueOf(userId));
            MDC.put(LOG_COMMAND_TYPE, CREATE.getValue());
            MDC.put(LOG_COMMAND_STATE, INGREDIENTS_UPDATE.getValue());
            MDC.put(LOG_DISH_NAME, dishName);
            logger.info("User: {} updated context: {}", userId, INGREDIENTS_UPDATE.getValue());
            MDC.clear();

        } catch (Exception e) {
            sendSilently(BotMessages.DISH_ALREADY_EXISTS, update);
            logger.error(e.getMessage());
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
            final String dishName = userContextDTO.getDishName();
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

                        MDC.put(LOG_USER_ID, String.valueOf(userId));
                        MDC.put(LOG_COMMAND_TYPE, CREATE.getValue());
                        MDC.put(LOG_COMMAND_STATE, DISH_NAME_UPDATE.getValue());
                        MDC.put(LOG_DISH_NAME, dishName);
                        logger.info("User: {} wrote ingredients for {} dish", userId, dishName);
                        MDC.clear();

                        dbDriver.updateUserContext(
                            new UserContextUpdateOptions(
                                userId,
                                RECIPE_UPDATE,
                                userDishName
                            )
                        );
                    }
            );
            sendSilently(BotMessages.WRITE_RECIPE, update);

            MDC.put(LOG_USER_ID, String.valueOf(userId));
            MDC.put(LOG_COMMAND_TYPE, CREATE.getValue());
            MDC.put(LOG_COMMAND_STATE, RECIPE_UPDATE.getValue());
            MDC.put(LOG_DISH_NAME, dishName);
            logger.info("User: {} updated context: {}", userId, RECIPE_UPDATE.getValue());
            MDC.clear();

        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            logger.error(e.getMessage());
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

                        MDC.put(LOG_USER_ID, String.valueOf(userId));
                        MDC.put(LOG_COMMAND_TYPE, CREATE.getValue());
                        MDC.put(LOG_COMMAND_STATE, DISH_NAME_UPDATE.getValue());
                        MDC.put(LOG_DISH_NAME, dishName);
                        logger.info("User: {} wrote recipe for {} dish", userId, dishName);
                        MDC.clear();

                        dbDriver.deleteUserContext(
                            new UserContextDeleteOptions(userId)
                        );
                    }
            );
            sendSilently(BotMessages.DISH_WAS_CREATED_WITH_SUCCESS, update);

            MDC.put(LOG_USER_ID, String.valueOf(userId));
            logger.info("User: {} ended CreateDishCommand", userId);
            MDC.clear();

        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            logger.error(e.getMessage());
        }
    }
}