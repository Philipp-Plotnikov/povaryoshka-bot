package telegram.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;

import static models.commands.CommandConfig.CREATE_DISH_COMMAND_SETTINGS;
import static models.commands.CommandStates.DISH_NAME;
import static models.commands.CommandStates.INGREDIENTS;
import static models.commands.CommandStates.RECIPE;
import static models.commands.MultiStateCommandTypes.CREATE;
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

public class CreateDishCommand implements AbilityExtension {
    @NonNull
    private final PovaryoshkaBot povaryoshkaBot;

    public CreateDishCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
    }

    // TODO: Try to reuse user select from isInCreateDishContext
    // TODO: Replace switch to map
    // TODO: Read about switch
    @NonNull
    public Ability createDish() {
        return Ability.builder()
            .name(CREATE_DISH_COMMAND_SETTINGS.commandName())
            .info(CREATE_DISH_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL) // ?
            .action(ctx -> {
                try {
                    povaryoshkaBot.getSilent().send("Напиши название блюда", ctx.chatId());
                    povaryoshkaBot.getDbDriver().insertUserContext(
                            new UserContextInsertOptions(
                                    ctx.user().getId(),
                                    CREATE,
                                    DISH_NAME,
                                    null
                            )
                    );
                } catch (SQLException e) {
                }
            })
            .reply((action, update) -> {
                    try {
                        final UserContextDTO userContextDTO = povaryoshkaBot.getDbDriver().selectUserContext(
                            new UserContextSelectOptions(
                                update.getMessage().getFrom().getId()
                            )
                        );
                        switch (userContextDTO.getCommandState()) {
                            case DISH_NAME -> handleDishNameState(update);
                            case INGREDIENTS -> handleIngredientsState(update, userContextDTO);
                            case RECIPE -> handleRecipeState(update, userContextDTO);
                            default -> throw new Exception();
                        }
                    } catch (Exception e) {
                    }
                },
                isInCreateDishContext()
            )
            .build();
    }

    private void handleDishNameState(@NonNull final Update update) {
        try {
            povaryoshkaBot.getDbDriver().updateUserContext(
                new UserContextUpdateOptions(
                    update.getMessage().getFrom().getId(),
                    INGREDIENTS,
                    update.getMessage().getText().trim()
                )
            );
            povaryoshkaBot.getDbDriver().insertDish(
                new DishInsertOptions(
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getText().trim(),
                    new ArrayList<>(),
                    null
                )
            );
            povaryoshkaBot.getSilent().send("Напишите ингредиенты", update.getMessage().getChatId());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void handleIngredientsState(
        @NonNull final Update update,
        @NonNull final UserContextDTO userContextDTO
    ) {
        try {
            povaryoshkaBot.getDbDriver().updateUserContext(
                new UserContextUpdateOptions(
                    update.getMessage().getFrom().getId(),
                    RECIPE,
                    userContextDTO.getDishName()
                )
            );
            final List<String> ingredientList = Collections.unmodifiableList(
                Arrays.asList(update.getMessage().getText().trim().split(", "))
            );
            povaryoshkaBot.getDbDriver().updateDish(
                new DishUpdateOptions(
                    update.getMessage().getFrom().getId(),
                    userContextDTO.getDishName(),
                    ingredientList,
                    null
                )
            );
            povaryoshkaBot.getSilent().send("Напишите рецепт", update.getMessage().getChatId());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void handleRecipeState(
        @NonNull final Update update,
        @NonNull final UserContextDTO userContextDTO
    ) {
        try {
            povaryoshkaBot.getDbDriver().deleteUserContext(
                new UserContextDeleteOptions(update.getMessage().getFrom().getId())
            );
            final DishDTO dishDTO = povaryoshkaBot.getDbDriver().selectDish(
                new DishSelectOptions(update.getMessage().getFrom().getId(), userContextDTO.getDishName())
            );
            povaryoshkaBot.getDbDriver().updateDish(
                new DishUpdateOptions(
                    update.getMessage().getFrom().getId(),
                    userContextDTO.getDishName(),
                    dishDTO.getIngredientList(),
                    update.getMessage().getText().trim()
                )
            );
            povaryoshkaBot.getSilent().send("Все заебись", update.getMessage().getChatId());
        } catch(Exception e) {
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
                final UserContextDTO userContextDTO = povaryoshkaBot.getDbDriver().selectUserContext(
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