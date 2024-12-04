package telegram.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;
import org.telegram.telegrambots.meta.api.objects.Update;

import language.ru.BotMessages;

import static models.commands.CommandConfig.CREATE_DISH_COMMAND_SETTINGS;
import static models.commands.CommandStates.DISH_NAME;
import static models.commands.CommandStates.INGREDIENTS;
import static models.commands.CommandStates.RECIPE;
import static models.commands.MultiStateCommandTypes.CREATE;

import models.commands.CommandStateHandler;
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


public class CreateDishCommand extends AbstractCommand {
    @NonNull
    private final EnumMap<@NonNull CommandStates, CommandStateHandler> stateHandlersMap = new EnumMap<>(CommandStates.class);

    public CreateDishCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        super(povaryoshkaBot);
        initStateHandlersMap();
    }

    private void initStateHandlersMap() {
        stateHandlersMap.put(DISH_NAME, this::handleDishNameState);
        stateHandlersMap.put(INGREDIENTS, this::handleIngredientsState);
        stateHandlersMap.put(RECIPE, this::handleRecipeState);
    }

    // TODO: Try to reuse user select from isInCreateDishContext
    @NonNull
    public Ability createDish() {
        return Ability.builder()
            .name(CREATE_DISH_COMMAND_SETTINGS.commandName())
            .info(CREATE_DISH_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL) // ?
            .action(ctx -> {
                final Update update = ctx.update();
                try {
                    sendSilently(BotMessages.WRITE_DISH_NAME, update);
                    dbDriver.insertUserContext(
                        new UserContextInsertOptions(
                            ctx.user().getId(),
                                CREATE,
                                DISH_NAME,
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
                            final CommandStateHandler commandStateHandler = stateHandlersMap.get(commandState);
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
                isInCreateDishContext()
            )
            .build();
    }

    private void handleDishNameState(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO)
    {
        try {
            dbDriver.updateUserContext(
                new UserContextUpdateOptions(
                    update.getMessage().getFrom().getId(),
                    INGREDIENTS,
                    update.getMessage().getText().trim()
                )
            );
            dbDriver.insertDish(
                new DishInsertOptions(
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getText().trim(),
                    new ArrayList<>(),
                    null
                )
            );
            povaryoshkaBot.getSilent().send(BotMessages.WRITE_INGREDIENTS, update.getMessage().getChatId());
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            System.out.println(e);
        }
    }

    private void handleIngredientsState(
        @NonNull final Update update,
        @NonNull final UserContextDTO userContextDTO
    ) {
        try {
            dbDriver.updateUserContext(
                new UserContextUpdateOptions(
                    update.getMessage().getFrom().getId(),
                    RECIPE,
                    userContextDTO.getDishName()
                )
            );
            final List<String> ingredientList = Collections.unmodifiableList(
                Arrays.asList(update.getMessage().getText().trim().split(", "))
            );
            dbDriver.updateDish(
                new DishUpdateOptions(
                    update.getMessage().getFrom().getId(),
                    userContextDTO.getDishName(),
                    null,
                    ingredientList,
                    null
                )
            );
            povaryoshkaBot.getSilent().send(BotMessages.WRITE_RECIPE, update.getMessage().getChatId());
        } catch (Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            System.out.println(e);
        }
    }

    private void handleRecipeState(
        @NonNull final Update update,
        @NonNull final UserContextDTO userContextDTO
    ) {
        try {
            dbDriver.deleteUserContext(
                new UserContextDeleteOptions(update.getMessage().getFrom().getId())
            );
            final DishDTO dishDTO = dbDriver.selectDish(
                new DishSelectOptions(update.getMessage().getFrom().getId(), userContextDTO.getDishName())
            );
            dbDriver.updateDish(
                new DishUpdateOptions(
                    update.getMessage().getFrom().getId(),
                    userContextDTO.getDishName(),
                    null,
                    dishDTO.getIngredientList(),
                    update.getMessage().getText().trim()
                )
            );
            povaryoshkaBot.getSilent().send(BotMessages.DISH_WAS_CREATED_WITH_SUCCESS, update.getMessage().getChatId());
        } catch(Exception e) {
            sendSilently(BotMessages.SOMETHING_WENT_WRONG, update);
            System.out.println(e);
        }
    }

    @NonNull
    private Predicate<Update> isInCreateDishContext() {
        return update -> {
            boolean isCreateDishContext = false;
            if (update.getMessage().getText().equals("/end")){
                return false;
            }
            try {
                final UserContextDTO userContextDTO = dbDriver.selectUserContext(
                    new UserContextSelectOptions(
                        update.getMessage().getFrom().getId()
                    )
                );
                if (userContextDTO != null && userContextDTO.getMultiStateCommandTypes() == CREATE) {
                    isCreateDishContext = true; 
                }
            } catch (SQLException e) {
                System.out.println(e);
            }
            return isCreateDishContext;
        };
    }
}