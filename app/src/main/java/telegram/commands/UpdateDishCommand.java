package telegram.commands;

import models.commands.CommandStateHandler;
import models.commands.CommandStates;
import models.db.sqlops.dish.DishUpdateOptions;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import models.db.sqlops.usercontext.UserContextInsertOptions;
import models.db.sqlops.usercontext.UserContextSelectOptions;
import models.db.sqlops.usercontext.UserContextUpdateOptions;
import models.dtos.UserContextDTO;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.meta.api.objects.Update;

import language.ru.BotMessages;
import telegram.bot.PovaryoshkaBot;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;

import static models.commands.CommandConfig.UPDATE_DISH_COMMAND_SETTINGS;
import static models.commands.CommandStates.*;
import static models.commands.MultiStateCommandTypes.*;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;


public class UpdateDishCommand extends AbstractCommand {
    @NonNull
    private final EnumMap<@NonNull CommandStates, CommandStateHandler> stateHandlersMap = new EnumMap<>(CommandStates.class);

    public UpdateDishCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
        initStateHandlersMap();
    }

    private void initStateHandlersMap() {
        stateHandlersMap.put(DISH_NAME, this::handleDishNameState);
        stateHandlersMap.put(INGREDIENTS_UPDATE_CONFIRM, this::handleIngredientsUpdateConfirmState);
        stateHandlersMap.put(INGREDIENTS, this::handleIngredientsState);
        stateHandlersMap.put(RECIPE_UPDATE_CONFIRM, this::handleRecipeUpdateConfirmState);
        stateHandlersMap.put(RECIPE, this::handleRecipeState);
    }

    @NonNull
    public Ability updateDish() {
        return Ability.builder()
            .name(UPDATE_DISH_COMMAND_SETTINGS.commandName())
            .info(UPDATE_DISH_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL) // ?
            .action(ctx -> {
                final Update update = ctx.update();
                try {
                    final String message = getUserDishList(ctx);
                    if (message == null) {
                        sendSilently(BotMessages.USER_DOES_NOT_HAVE_DISHES, update);
                        return;
                    }
                    sendSilently(message, update);
                    sendSilently(BotMessages.WRITE_DISH_NAME_FROM_LIST_TO_DELETE, update);
                    dbDriver.insertUserContext(
                        new UserContextInsertOptions(
                            ctx.user().getId(),
                            UPDATE,
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
                    final UserContextDTO userContextDTO = dbDriver.selectUserContext(
                        new UserContextSelectOptions(
                            update.getMessage().getFrom().getId()
                        )
                    );
                    if (userContextDTO != null) {
                        final CommandStates commandState = userContextDTO.getCommandState();
                        final CommandStateHandler commandStateHandler = stateHandlersMap.get(commandState);
                        if (commandStateHandler == null) {
                            throw new Exception("Dont have needed handlers");
                        }
                        commandStateHandler.handle(update, userContextDTO);
                    }
                    } catch(Exception e) {
                        sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
                        System.out.println("Ошибка обновления блюда: " + e.getMessage());
                    }
                },
                Flag.TEXT,
                isUpdateContext()
            )
            .build();
    }

    private void handleDishNameState(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO) {
        try {
            dbDriver.updateUserContext(
                new UserContextUpdateOptions(
                    update.getMessage().getFrom().getId(),
                    INGREDIENTS_UPDATE_CONFIRM,
                    update.getMessage().getText().trim()
                )
            );
            sendSilently(BotMessages.CONFIRM_INGREDIENTS_UPDATE, update);
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            System.out.println(e);
        }
    }

    private void handleIngredientsUpdateConfirmState(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO) {
        try {
            final long userId = update.getMessage().getFrom().getId();
            if (update.getMessage().getText().trim().equalsIgnoreCase("Нет")) {
                sendSilently(BotMessages.INGREDIENTS_ARE_NOT_UPDATED, update);
                dbDriver.updateUserContextCommandState(new UserContextUpdateOptions(
                    userId,
                    RECIPE_UPDATE_CONFIRM,
                    null
                ));
                return;
            }
            sendSilently(BotMessages.INPUT_NEW_INGREDIENTS, update);
            dbDriver.updateUserContextCommandState(new UserContextUpdateOptions(
                userId,
                INGREDIENTS,
                null
            ));
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            System.out.println(e);
        }
    }

    private void handleIngredientsState(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO) {
        try {
            final long userId = update.getMessage().getFrom().getId();
            final List<String> ingredientList = Collections.unmodifiableList(
                Arrays.asList(update.getMessage().getText().trim().split(", "))
            );
            dbDriver.executeAsTransaction(
                () -> {
                    dbDriver.updateDishIngredientList(
                        new DishUpdateOptions(
                            userId,
                            userContextDTO.getDishName(),
                            null,
                            ingredientList,
                            null
                        )
                    );
                    dbDriver.updateUserContextCommandState(
                        new UserContextUpdateOptions(
                            userId,
                            RECIPE_UPDATE_CONFIRM,
                            null
                        )
                    );
                }
            );
            sendSilently(BotMessages.CONFIRM_RECIPE_UPDATE, update);
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            System.out.println(e);
        }
    }

    private void handleRecipeUpdateConfirmState(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO) {
        try {
            final long userId = update.getMessage().getFrom().getId();
            if (update.getMessage().getText().trim().equalsIgnoreCase("Нет")) {
                sendSilently(BotMessages.RECIPE_IS_NOT_UPDATED, update);
                dbDriver.deleteUserContext(
                    new UserContextDeleteOptions(
                        userId
                    )
                );
                return;
            }
            sendSilently(BotMessages.INPUT_NEW_RECIPE, update);
            dbDriver.updateUserContextCommandState(
                new UserContextUpdateOptions(
                    userId,
                    RECIPE,
                    null
                )
            );
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            System.out.println(e);
        }
    }

    private void handleRecipeState(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO) {
        try {
            final long userId = update.getMessage().getFrom().getId();
            final String recipe = update.getMessage().getText().trim();
            dbDriver.executeAsTransaction(
                () -> {
                    dbDriver.updateDishRecipe(
                        new DishUpdateOptions(
                            userId,
                            userContextDTO.getDishName(),
                            null,
                            null,
                            recipe
                        )
                    );
                    dbDriver.deleteUserContext(
                        new UserContextDeleteOptions(
                            userId
                        )
                    );
                }
            );
            sendSilently(BotMessages.DISH_WAS_UPDATED_WITH_SUCCESS, update);
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            System.out.println(e);
        }
    }

    private Predicate<Update> isUpdateContext(){
        return update -> {
            boolean isUpdateContext = false;
            if (update.getMessage().getText().equals("/end")){
                return false;
            }
            try {
                final UserContextDTO userContextDTO = dbDriver.selectUserContext(
                    new UserContextSelectOptions(
                        update.getMessage().getFrom().getId()
                    )
                );
                if (userContextDTO != null && userContextDTO.getMultiStateCommandTypes() == UPDATE) {
                    isUpdateContext = true;
                }
            } catch(SQLException e) {
                System.out.println(e);
            }
            return isUpdateContext;
        };
    }
}