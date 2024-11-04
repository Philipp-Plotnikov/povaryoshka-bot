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
                    povaryoshkaBot.getDbDriver().insertUserContext(
                            new UserContextInsertOptions(
                                    ctx.user().getId(),
                                    UPDATE,
                                    DISH_NAME,
                                    null
                            )
                    );
                    List<DishDTO> dishes = povaryoshkaBot.getDbDriver().selectDishList(
                            new DishListSelectOptions(ctx.user().getId())
                    );
                    if (dishes.isEmpty()) {
                        povaryoshkaBot.getSilent().send("У вас нет сохраненных блюд", ctx.chatId());
                        return;
                    }
                    StringBuilder message = new StringBuilder("Ваши блюда:\n");
                    for (DishDTO dish : dishes) {
                        message.append("- ").append(dish.getName()).append("\n");
                    }

                    povaryoshkaBot.getSilent().send(message.toString(), ctx.chatId());
                    povaryoshkaBot.getSilent().send("Напишите название блюда из списка, которое хотите обновить", ctx.chatId());

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
                                if (userContextDTO.getMultiStateCommandTypes() == UPDATE) {
                                    String dishName = update.getMessage().getText().trim();

                                    // Проверка, есть ли такое блюдо в списке
                                    boolean dishFound = false;
                                    for (DishDTO dish : povaryoshkaBot.getDbDriver().selectDishList(
                                            new DishListSelectOptions(update.getMessage().getFrom().getId())
                                    )) {
                                        if (dish.getName().equalsIgnoreCase(dishName)) {
                                            dishFound = true;
                                            break;
                                        }
                                    }

                                    if (dishFound) {
                                        DishDTO selectedDish = povaryoshkaBot.getDbDriver().selectDish(
                                                new DishSelectOptions(update.getMessage().getFrom().getId(), dishName)
                                        );

                                        // Выводим информацию о блюде (название, ингредиенты, рецепт)
                                        String message = "Название: " + selectedDish.getName() + "\n" +
                                                "Ингредиенты: " + (selectedDish.getIngredientList() != null ? String.join(", ", selectedDish.getIngredientList()) : "Нет информации") + "\n" +
                                                "Рецепт: " + (selectedDish.getRecipe() != null ? selectedDish.getRecipe() : "Нет информации");

                                        povaryoshkaBot.getSilent().send(message, update.getMessage().getChatId());
                                        povaryoshkaBot.getSilent().send("Вот информация о блюде, которое вы хотите обновить", update.getMessage().getChatId());
                                        povaryoshkaBot.getSilent().send("Обновляем список ингредиентов?(Да/Нет)", update.getMessage().getChatId());

                                        povaryoshkaBot.getDbDriver().updateUserContext(
                                                new UserContextUpdateOptions(
                                                        update.getMessage().getFrom().getId(),
                                                        INGREDIENTS,
                                                        dishName
                                                )
                                        );
                                        if (userContextDTO.getCommandState() == INGREDIENTS) {
                                            if (update.getMessage().getText().trim().equalsIgnoreCase("Нет")){
                                                povaryoshkaBot.getSilent().send("Ингредиенты не меняем", update.getMessage().getChatId());
                                            }
                                            else {
                                                povaryoshkaBot.getSilent().send("Введите новые ингредиенты", update.getMessage().getChatId());

                                                final List<String> ingredientList = Collections.unmodifiableList(
                                                    Arrays.asList(update.getMessage().getText().trim().split(", "))
                                                );
                                                povaryoshkaBot.getDbDriver().updateDishIngredientList(
                                                    new DishUpdateOptions(
                                                            update.getMessage().getFrom().getId(),
                                                            dishName,
                                                            ingredientList,
                                                            null
                                                    )
                                                );
                                            }
                                            povaryoshkaBot.getDbDriver().updateUserContext(
                                                    new UserContextUpdateOptions(
                                                            update.getMessage().getFrom().getId(),
                                                            RECIPE,
                                                            dishName
                                                    )
                                            );
                                        }
                                        if (userContextDTO.getCommandState() == RECIPE) {
                                            povaryoshkaBot.getSilent().send("Обновляем рецепт блюда?(Да/Нет)", update.getMessage().getChatId());

                                            if (update.getMessage().getText().trim().equalsIgnoreCase("Нет")){
                                                povaryoshkaBot.getSilent().send("Рецепт не меняем", update.getMessage().getChatId());
                                            }
                                            else {
                                                povaryoshkaBot.getSilent().send("Введите новый рецепт", update.getMessage().getChatId());

                                                final String recipe = update.getMessage().getText().trim();
                                                povaryoshkaBot.getDbDriver().updateDishRecipe(
                                                        new DishUpdateOptions(
                                                                update.getMessage().getFrom().getId(),
                                                                dishName,
                                                                null,
                                                                recipe
                                                        )
                                                );
                                            }
                                            povaryoshkaBot.getDbDriver().deleteUserContext(
                                                    new UserContextDeleteOptions(
                                                            update.getMessage().getFrom().getId()
                                                    )
                                            );
                                            povaryoshkaBot.getSilent().send("Блюдо обновлено", update.getMessage().getChatId());
                                        }
                                    } else {
                                        povaryoshkaBot.getSilent().send("Такого блюда нет в списке. Попробуйте снова.", update.getMessage().getChatId());
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
    private Predicate<Update> isUpdateContext(){
        return update -> {
            boolean isUpdateContext = false;
            try {
                final UserContextDTO userContextDTO = povaryoshkaBot.getDbDriver().selectUserContext(
                        new UserContextSelectOptions(
                                update.getMessage().getFrom().getId()
                        )
                );
                if (userContextDTO.getMultiStateCommandTypes() == UPDATE) {
                    isUpdateContext = true;
                }
            } catch(SQLException e) {
                System.out.println(e);
            }
            return isUpdateContext;

        };
    }
}