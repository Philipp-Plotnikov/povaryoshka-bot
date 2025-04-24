package telegram.commands;

import models.commands.ICommandStateHandler;
import models.commands.CommandStates;
import models.db.sqlops.dish.DishSelectOptions;
import models.db.sqlops.dish.DishUpdateOptions;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import models.db.sqlops.usercontext.UserContextInsertOptions;
import models.db.sqlops.usercontext.UserContextSelectOptions;
import models.db.sqlops.usercontext.UserContextUpdateOptions;
import models.dtos.DishDTO;
import models.dtos.UserContextDTO;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.meta.api.objects.Update;

import language.ru.BotMessages;
import language.ru.UserMessages;
import telegram.bot.PovaryoshkaBot;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import static models.commands.CommandConfig.UPDATE_DISH_COMMAND_SETTINGS;
import static models.commands.CommandStates.*;
import static models.commands.MultiStateCommandTypes.*;
import static models.logger.LoggerFields.*;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;


final public class UpdateDishCommand extends AbstractCommand {
    @NonNull
    private final EnumMap<@NonNull CommandStates, ICommandStateHandler> stateHandlersMap = new EnumMap<>(CommandStates.class);

    @NonNull
    private final Logger logger = LoggerFactory.getLogger(UpdateDishCommand.class);

    public UpdateDishCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
        initStateHandlersMap();
    }

    private void initStateHandlersMap() {
        stateHandlersMap.put(DISH_NAME, this::handleDishNameState);
        stateHandlersMap.put(INGREDIENTS_UPDATE_CONFIRM, this::handleIngredientsUpdateConfirmState);
        stateHandlersMap.put(INGREDIENTS_UPDATE, this::handleIngredientsUpdateState);
        stateHandlersMap.put(RECIPE_UPDATE_CONFIRM, this::handleRecipeUpdateConfirmState);
        stateHandlersMap.put(RECIPE_UPDATE, this::handleRecipeUpdateState);
        stateHandlersMap.put(DISH_NAME_UPDATE_CONFIRM, this::handleDishNameUpdateConfirmState);
        stateHandlersMap.put(DISH_NAME_UPDATE, this::handleDishNameUpdateState);
    }

    @NonNull
    public Ability updateDish() {
        return Ability.builder()
            .name(UPDATE_DISH_COMMAND_SETTINGS.commandName())
            .info(UPDATE_DISH_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL)
            .action(ctx -> {
                final Update update = ctx.update();
                final long userId = ctx.user().getId();

                MDC.put(LOG_USER_ID, String.valueOf(userId));
                logger.info("User: {} used UpdateDishCommand", userId);

                try {
                    final String message = getUserDishList(ctx);
                    if (message == null) {
                        sendSilently(BotMessages.USER_DOES_NOT_HAVE_DISHES, update);
                        return;
                    }
                    sendSilently(message, update);
                    sendSilently(BotMessages.WRITE_DISH_NAME_FROM_LIST_TO_UPDATE, update);
                    dbDriver.insertUserContext(
                        new UserContextInsertOptions(
                            userId,
                            UPDATE,
                            DISH_NAME,
                            null
                        )
                    );

                    MDC.put(LOG_COMMAND_TYPE, UPDATE.getValue());
                    MDC.put(LOG_COMMAND_STATE, DISH_NAME.getValue());
                    MDC.put(LOG_DISH_NAME, null);
                    logger.info("User: {} inserted context in UpdateDishCommand", userId);
                    MDC.clear();

                } catch(SQLException e) {
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
                    } catch(Exception e) {
                        sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                        logger.error(e.getMessage());
                    }
                },
                Flag.TEXT,
                isSpecifiedContext(UPDATE)
            )
            .build();
    }

    public void handleDishNameState(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO) {
        try {
            final long userId = update.getMessage().getFrom().getId();
            final String dishName = update.getMessage().getText().trim();
            if (!isDishNameExist(userId, dishName)) {
                sendSilently(BotMessages.THIS_DISH_NAME_IS_NOT_FROM_LIST, update);
                return;
            }
            dbDriver.updateUserContext(
                new UserContextUpdateOptions(
                    userId,
                    DISH_NAME_UPDATE_CONFIRM,
                    dishName
                )
            );

            MDC.put(LOG_USER_ID, String.valueOf(userId));
            MDC.put(LOG_COMMAND_TYPE, UPDATE.getValue());
            MDC.put(LOG_COMMAND_STATE, DISH_NAME_UPDATE_CONFIRM.getValue());
            MDC.put(LOG_DISH_NAME, dishName);
            logger.info("User: {} updated context: {}", userId, DISH_NAME_UPDATE_CONFIRM.getValue());
            MDC.clear();

            sendSilently(BotMessages.CONFIRM_DISH_NAME_UPDATE, update);
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            logger.error(e.getMessage());
        }
    }

    public void handleDishNameUpdateConfirmState(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO) {
        try {
            final long userId = update.getMessage().getFrom().getId();
            final String userMessage = update.getMessage().getText().trim();
            final String dishName = userContextDTO.getDishName();
            if (userMessage.equalsIgnoreCase(UserMessages.NO)) {
                dbDriver.updateUserContextCommandState(
                    new UserContextUpdateOptions(
                        userId,
                        INGREDIENTS_UPDATE_CONFIRM,
                        null
                    )
                );

                MDC.put(LOG_USER_ID, String.valueOf(userId));
                MDC.put(LOG_COMMAND_TYPE, UPDATE.getValue());
                MDC.put(LOG_COMMAND_STATE, INGREDIENTS_UPDATE_CONFIRM.getValue());
                MDC.put(LOG_DISH_NAME, dishName);
                logger.info("User: {} updated command state: {}", userId, INGREDIENTS_UPDATE_CONFIRM.getValue());
                MDC.clear();

                sendSilently(BotMessages.DISH_NAME_IS_NOT_UPDATED, update);
                sendSilently(BotMessages.CONFIRM_INGREDIENTS_UPDATE, update);
                return;
            }
            if (userMessage.equalsIgnoreCase(UserMessages.YES)){
                dbDriver.updateUserContextCommandState(
                    new UserContextUpdateOptions(
                        userId,
                        DISH_NAME_UPDATE,
                        null
                    )
                );

                MDC.put(LOG_USER_ID, String.valueOf(userId));
                MDC.put(LOG_COMMAND_TYPE, UPDATE.getValue());
                MDC.put(LOG_COMMAND_STATE, DISH_NAME_UPDATE.getValue());
                MDC.put(LOG_DISH_NAME, dishName);
                logger.info("User: {} updated command state: {}", userId, DISH_NAME_UPDATE.getValue());
                MDC.clear();

                sendSilently(BotMessages.INPUT_NEW_DISH_NAME, update);
                return;
            }
            sendSilently(BotMessages.ENTER_YES_OR_NO, update);
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            logger.error(e.getMessage());
        }
    }

    public void handleDishNameUpdateState(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO) {
        try {
            final long userId = update.getMessage().getFrom().getId();
            final String newDishName = update.getMessage().getText().trim();
            final String dishName = userContextDTO.getDishName();
            dbDriver.executeAsTransaction(
                () -> {
                    dbDriver.updateDishName(
                        new DishUpdateOptions(
                            userId,
                            dishName,
                            newDishName,
                            null,
                            null
                        )
                    );

                    MDC.put(LOG_USER_ID, String.valueOf(userId));
                    MDC.put(LOG_COMMAND_TYPE, UPDATE.getValue());
                    MDC.put(LOG_COMMAND_STATE, DISH_NAME_UPDATE.getValue());
                    MDC.put(LOG_DISH_NAME, newDishName);
                    logger.info("User: {} updated dish name: {} -> {}", userId, dishName, newDishName);
                    MDC.clear();

                    dbDriver.updateUserContext(
                        new UserContextUpdateOptions(
                            userId,
                            INGREDIENTS_UPDATE_CONFIRM,
                            newDishName
                        )
                    );

                    MDC.put(LOG_USER_ID, String.valueOf(userId));
                    MDC.put(LOG_COMMAND_TYPE, UPDATE.getValue());
                    MDC.put(LOG_COMMAND_STATE, INGREDIENTS_UPDATE_CONFIRM.getValue());
                    MDC.put(LOG_DISH_NAME, newDishName);
                    logger.info("User: {} updated context: {}", userId, INGREDIENTS_UPDATE_CONFIRM.getValue());
                    MDC.clear();

                }
            );
            sendSilently(BotMessages.CONFIRM_INGREDIENTS_UPDATE, update);
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            logger.error(e.getMessage());
        }
    }

    private boolean isDishNameExist(final long userId, final String dishName) throws SQLException {
        final DishDTO dishDTO = dbDriver.selectDish(
            new DishSelectOptions(userId, dishName)
        );
        return dishDTO != null ? true : false;
    }

    public void handleIngredientsUpdateConfirmState(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO) {
        try {
            final long userId = update.getMessage().getFrom().getId();
            final String userMessage = update.getMessage().getText().trim();
            final String dishName = userContextDTO.getDishName();
            if (userMessage.equalsIgnoreCase(UserMessages.NO)) {
                dbDriver.updateUserContextCommandState(
                    new UserContextUpdateOptions(
                        userId,
                        RECIPE_UPDATE_CONFIRM,
                        null
                    )
                );

                MDC.put(LOG_USER_ID, String.valueOf(userId));
                MDC.put(LOG_COMMAND_TYPE, UPDATE.getValue());
                MDC.put(LOG_COMMAND_STATE, RECIPE_UPDATE_CONFIRM.getValue());
                MDC.put(LOG_DISH_NAME, dishName);
                logger.info("User: {} updated command state: {}", userId, RECIPE_UPDATE_CONFIRM.getValue());
                MDC.clear();

                sendSilently(BotMessages.INGREDIENTS_ARE_NOT_UPDATED, update);
                sendSilently(BotMessages.CONFIRM_RECIPE_UPDATE, update);
                return;
            }
            if (userMessage.equalsIgnoreCase(UserMessages.YES)){
                dbDriver.updateUserContextCommandState(
                    new UserContextUpdateOptions(
                        userId,
                        INGREDIENTS_UPDATE,
                        null
                    )
                );

                MDC.put(LOG_USER_ID, String.valueOf(userId));
                MDC.put(LOG_COMMAND_TYPE, UPDATE.getValue());
                MDC.put(LOG_COMMAND_STATE, INGREDIENTS_UPDATE.getValue());
                MDC.put(LOG_DISH_NAME, dishName);
                logger.info("User: {} updated command state: {}", userId, INGREDIENTS_UPDATE.getValue());
                MDC.clear();

                sendSilently(BotMessages.INPUT_NEW_INGREDIENTS, update);
                return;
            }
            sendSilently(BotMessages.ENTER_YES_OR_NO, update);
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            logger.error(e.getMessage());
        }
    }

    public void handleIngredientsUpdateState(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO) {
        try {
            final long userId = update.getMessage().getFrom().getId();
            final List<String> ingredientList = Collections.unmodifiableList(
                Arrays.asList(update.getMessage().getText().trim().split(", "))
            );
            final String dishName = userContextDTO.getDishName();
            dbDriver.executeAsTransaction(
                () -> {
                    dbDriver.updateDishIngredientList(
                        new DishUpdateOptions(
                            userId,
                            dishName,
                            null,
                            ingredientList,
                            null
                        )
                    );

                    MDC.put(LOG_USER_ID, String.valueOf(userId));
                    MDC.put(LOG_COMMAND_TYPE, UPDATE.getValue());
                    MDC.put(LOG_COMMAND_STATE, INGREDIENTS_UPDATE.getValue());
                    MDC.put(LOG_DISH_NAME, dishName);
                    logger.info("User: {} updated ingredients for {} dish", userId, dishName);
                    MDC.clear();

                    dbDriver.updateUserContextCommandState(
                        new UserContextUpdateOptions(
                            userId,
                            RECIPE_UPDATE_CONFIRM,
                            null
                        )
                    );

                    MDC.put(LOG_USER_ID, String.valueOf(userId));
                    MDC.put(LOG_COMMAND_TYPE, UPDATE.getValue());
                    MDC.put(LOG_COMMAND_STATE, RECIPE_UPDATE_CONFIRM.getValue());
                    MDC.put(LOG_DISH_NAME, dishName);
                    logger.info("User: {} updated command state: {}", userId, RECIPE_UPDATE_CONFIRM.getValue());
                    MDC.clear();

                }
            );
            sendSilently(BotMessages.CONFIRM_RECIPE_UPDATE, update);
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            logger.error(e.getMessage());
        }
    }

    public void handleRecipeUpdateConfirmState(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO) {
        try {
            final long userId = update.getMessage().getFrom().getId();
            final String userMessage = update.getMessage().getText().trim();
            if (userMessage.equalsIgnoreCase(UserMessages.NO)) {
                dbDriver.deleteUserContext(
                    new UserContextDeleteOptions(userId)
                );
                sendSilently(BotMessages.RECIPE_IS_NOT_UPDATED, update);
                sendSilently(BotMessages.DISH_WAS_UPDATED_WITH_SUCCESS, update);

                MDC.put(LOG_USER_ID, String.valueOf(userId));
                logger.info("User: {} ended UpdateDishCommand", userId);
                MDC.clear();

                return;
            }
            if (userMessage.equalsIgnoreCase(UserMessages.YES)){
                dbDriver.updateUserContextCommandState(
                    new UserContextUpdateOptions(
                        userId,
                        RECIPE_UPDATE,
                        null
                    )
                );

                MDC.put(LOG_USER_ID, String.valueOf(userId));
                MDC.put(LOG_COMMAND_TYPE, UPDATE.getValue());
                MDC.put(LOG_COMMAND_STATE, RECIPE_UPDATE.getValue());
                MDC.put(LOG_DISH_NAME, userContextDTO.getDishName());
                logger.info("User: {} updated command state: {}", userId, RECIPE_UPDATE.getValue());
                MDC.clear();

                sendSilently(BotMessages.INPUT_NEW_RECIPE, update);
                return;
            }
            sendSilently(BotMessages.ENTER_YES_OR_NO, update);
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            logger.error(e.getMessage());
        }
    }

    public void handleRecipeUpdateState(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO) {
        try {
            final long userId = update.getMessage().getFrom().getId();
            final String recipe = update.getMessage().getText().trim();
            final String dishName = userContextDTO.getDishName();
            dbDriver.executeAsTransaction(
                () -> {
                    dbDriver.updateDishRecipe(
                        new DishUpdateOptions(
                            userId,
                            dishName,
                            null,
                            null,
                            recipe
                        )
                    );

                    MDC.put(LOG_USER_ID, String.valueOf(userId));
                    MDC.put(LOG_COMMAND_TYPE, UPDATE.getValue());
                    MDC.put(LOG_COMMAND_STATE, RECIPE_UPDATE.getValue());
                    MDC.put(LOG_DISH_NAME, dishName);
                    logger.info("User: {} updated recipe for {} dish", userId, dishName);
                    MDC.clear();

                    dbDriver.deleteUserContext(
                         new UserContextDeleteOptions(userId)
                    );
                }
            );
            sendSilently(BotMessages.DISH_WAS_UPDATED_WITH_SUCCESS, update);

            MDC.put(LOG_USER_ID, String.valueOf(userId));
            logger.info("User: {} ended UpdateDishCommand", userId);
            MDC.clear();

        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            logger.error(e.getMessage());
        }
    }
}