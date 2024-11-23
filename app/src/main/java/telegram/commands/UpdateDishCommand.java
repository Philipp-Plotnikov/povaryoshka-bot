package telegram.commands;

import models.db.sqlops.dish.DishInsertOptions;
import models.db.sqlops.dish.DishListSelectOptions;
import models.db.sqlops.dish.DishSelectOptions;
import models.db.sqlops.dish.DishUpdateOptions;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import models.db.sqlops.usercontext.UserContextInsertOptions;
import models.db.sqlops.usercontext.UserContextSelectOptions;
import models.db.sqlops.usercontext.UserContextUpdateOptions;
import models.dtos.DishDTO;
import models.dtos.UserContextDTO;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import telegram.bot.PovaryoshkaBot;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static models.commands.CommandConfig.UPDATE_DISH_COMMAND_SETTINGS;
import static models.commands.CommandStates.*;
import static models.commands.MultiStateCommandTypes.*;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

public class UpdateDishCommand implements AbilityExtension {
    @NonNull
    private final PovaryoshkaBot povaryoshkaBot;

    public UpdateDishCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
    }

    @NonNull
    public Ability updateDish() {
        return Ability.builder()
            .name(UPDATE_DISH_COMMAND_SETTINGS.commandName())
            .info(UPDATE_DISH_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL) // ?
            .action(ctx -> {
                try {

                    final List<DishDTO> dishes = povaryoshkaBot.getDbDriver().selectDishList(
                            new DishListSelectOptions(ctx.user().getId())
                    );
                    if (dishes == null) {
                        povaryoshkaBot.getSilent().send("У вас нет сохраненных блюд", ctx.chatId());
                        return;
                    }
                    StringBuilder message = new StringBuilder("Ваши блюда:\n");
                    for (DishDTO dish : dishes) {
                        message.append("- ").append(dish.getName()).append("\n");
                    }

                    povaryoshkaBot.getSilent().send(message.toString(), ctx.chatId());
                    povaryoshkaBot.getSilent().send("Напишите название блюда из списка, которое хотите обновить", ctx.chatId());
                    povaryoshkaBot.getDbDriver().insertUserContext(
                            new UserContextInsertOptions(
                                    ctx.user().getId(),
                                    UPDATE,
                                    DISH_NAME,
                                    null
                            )
                    );
                } catch(SQLException e) {
                    povaryoshkaBot.getSilent().send("Извините, произошла ошибка. Попробуйте позже.", ctx.chatId());
                    System.out.println("Ошибка при вставке: " + e.getMessage());
                }
            })
                .reply((action, update) -> {
                    try {
                        final UserContextDTO userContextDTO = povaryoshkaBot.getDbDriver().selectUserContext(
                                new UserContextSelectOptions(
                                        update.getMessage().getFrom().getId()
                                )
                        );
                        if (userContextDTO != null) {
                            switch (userContextDTO.getCommandState()) {
                                case DISH_NAME -> handleDishNameState(update);
                                case CONFIRM_INGREDIENT_UPDATE -> handleConfirmIngredientUpdate(update);
                                case INGREDIENTS -> updateIngredients(update, userContextDTO);
                                case CONFIRM_RECIPE_UPDATE -> handleConfirmRecipeUpdate(update);
                                case RECIPE -> updateRecipe(update, userContextDTO);
                                default -> throw new Exception();
                            }
                        }
                    } catch(Exception e) {
                        System.out.println("Ошибка обновления блюда: " + e.getMessage());
                    }
                    },
                        isUpdateContext()
                )
                .build();
    }

    private void handleDishNameState(@NonNull final Update update) {
        try {
            povaryoshkaBot.getDbDriver().updateUserContext(
                    new UserContextUpdateOptions(
                            update.getMessage().getFrom().getId(),
                            CONFIRM_INGREDIENT_UPDATE,
                            update.getMessage().getText().trim()
                    )
            );
            povaryoshkaBot.getSilent().send("Обновляем ингредиенты блюда?(Да/Нет)", update.getMessage().getChatId());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void handleConfirmIngredientUpdate(@NonNull final Update update) throws SQLException {
        if (update.getMessage().getText().trim().equalsIgnoreCase("Нет")){
            povaryoshkaBot.getSilent().send("Ингредиенты не меняем", update.getMessage().getChatId());
            povaryoshkaBot.getDbDriver().updateUserContextCommandState(new UserContextUpdateOptions(
                    update.getMessage().getFrom().getId(),
                    CONFIRM_RECIPE_UPDATE,
                    null
            ));
            return;
        }
        povaryoshkaBot.getSilent().send("Введите новые ингредиенты", update.getMessage().getChatId());
        povaryoshkaBot.getDbDriver().updateUserContextCommandState(new UserContextUpdateOptions(
                update.getMessage().getFrom().getId(),
                INGREDIENTS,
                null
        ));
    }

    private void updateIngredients(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO) throws SQLException, Exception {
        final List<String> ingredientList = Collections.unmodifiableList(
                Arrays.asList(update.getMessage().getText().trim().split(", "))
        );
        povaryoshkaBot.getDbDriver().executeAsTransaction(
                () -> {
                    povaryoshkaBot.getDbDriver().updateDishIngredientList(new DishUpdateOptions(
                                    update.getMessage().getFrom().getId(),
                                    userContextDTO.getDishName(),
                                    ingredientList,
                                    null
                            )
                    );

                    povaryoshkaBot.getDbDriver().updateUserContextCommandState(
                            new UserContextUpdateOptions(
                                    update.getMessage().getFrom().getId(),
                                    CONFIRM_RECIPE_UPDATE,
                                    null
                            )
                    );
                }
        );
        povaryoshkaBot.getSilent().send("Обновляем рецепт блюда?(Да/Нет)", update.getMessage().getChatId());
    }

    private void handleConfirmRecipeUpdate(@NonNull final Update update) throws SQLException {
        if (update.getMessage().getText().trim().equalsIgnoreCase("Нет")){
            povaryoshkaBot.getSilent().send(" Рецепт не меняем", update.getMessage().getChatId());
            povaryoshkaBot.getDbDriver().deleteUserContext(
                    new UserContextDeleteOptions(
                            update.getMessage().getFrom().getId()
                    )
            );
            return;
        }
        povaryoshkaBot.getSilent().send("Введите новый рецепт", update.getMessage().getChatId());
        povaryoshkaBot.getDbDriver().updateUserContextCommandState(new UserContextUpdateOptions(
                update.getMessage().getFrom().getId(),
                RECIPE,
                null
        ));
    }

    private void updateRecipe(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO) throws SQLException, Exception {
        final String recipe = update.getMessage().getText().trim();
        povaryoshkaBot.getDbDriver().executeAsTransaction(
                () -> {
                    povaryoshkaBot.getDbDriver().updateDishRecipe(
                            new DishUpdateOptions(
                                    update.getMessage().getFrom().getId(),
                                    userContextDTO.getDishName(),
                                    null,
                                    recipe
                            )
                    );
                    povaryoshkaBot.getDbDriver().deleteUserContext(
                            new UserContextDeleteOptions(
                                    update.getMessage().getFrom().getId()
                            )
                    );
                }
        );
        povaryoshkaBot.getSilent().send("Блюдо обновлено", update.getMessage().getChatId());
    }

    private Predicate<Update> isUpdateContext(){
        return update -> {
            boolean isUpdateContext = false;
            if (update.getMessage().getText().equals("/end")){
                return false;
            }
            try {
                final UserContextDTO userContextDTO = povaryoshkaBot.getDbDriver().selectUserContext(
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