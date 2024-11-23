package telegram.commands;

import models.db.sqlops.dish.DishDeleteOptions;
import models.db.sqlops.dish.DishListSelectOptions;
import models.db.sqlops.dish.DishSelectOptions;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import models.db.sqlops.usercontext.UserContextInsertOptions;
import models.db.sqlops.usercontext.UserContextSelectOptions;
import models.dtos.DishDTO;
import models.dtos.UserContextDTO;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import static models.commands.CommandStates.DISH_NAME;
import static models.commands.MultiStateCommandTypes.DELETE;
import static models.commands.MultiStateCommandTypes.GET;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import static models.commands.CommandConfig.GET_DISH_COMMAND_SETTINGS;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import telegram.bot.PovaryoshkaBot;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Predicate;

public class GetDishCommand implements AbilityExtension {
    @NonNull
    private final PovaryoshkaBot povaryoshkaBot;

    public GetDishCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
    }

    @NonNull
    public Ability getDish() {
        return Ability.builder()
            .name(GET_DISH_COMMAND_SETTINGS.commandName())
            .info(GET_DISH_COMMAND_SETTINGS.commandDescription())
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
                    final StringBuilder message = new StringBuilder("Ваши блюда:\n");
                    for (DishDTO dish : dishes) {
                        message.append("- ").append(dish.getName()).append("\n");
                    }

                    povaryoshkaBot.getSilent().send(message.toString(), ctx.chatId());
                    povaryoshkaBot.getSilent().send("Напишите название блюда из списка, которое хотите получить", ctx.chatId());
                    povaryoshkaBot.getDbDriver().insertUserContext(
                            new UserContextInsertOptions(
                                    ctx.user().getId(),
                                    GET,
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

                                String dishName = update.getMessage().getText().trim();
                                final List<DishDTO> listDishes = povaryoshkaBot.getDbDriver().selectDishList(
                                        new DishListSelectOptions(update.getMessage().getFrom().getId())
                                );

                                boolean dishFound = false;
                                if (listDishes != null){
                                    for (DishDTO dish : listDishes) {
                                        if (dish.getName().equalsIgnoreCase(dishName)) {
                                            dishFound = true;
                                            break;
                                        }
                                    }
                                }
                                if (!dishFound) {
                                    povaryoshkaBot.getSilent().send("Такого блюда нет в списке. Попробуйте снова.", update.getMessage().getChatId());
                                    return;
                                }
                                DishDTO selectedDish = povaryoshkaBot.getDbDriver().selectDish(
                                        new DishSelectOptions(update.getMessage().getFrom().getId(), dishName)
                                );

                                final String message = String.format("Название: %s\nИнгредиенты: %s\nРецепт: %s",
                                        selectedDish.getName(),
                                        (selectedDish.getIngredientList() != null ? String.join(", ", selectedDish.getIngredientList()) : "Нет информации"),
                                        (selectedDish.getRecipe() != null ? selectedDish.getRecipe() : "Нет информации"));
                                povaryoshkaBot.getSilent().send(message, update.getMessage().getChatId());
                                povaryoshkaBot.getDbDriver().deleteUserContext(
                                        new UserContextDeleteOptions(
                                                update.getMessage().getFrom().getId()
                                        )
                                );
                                povaryoshkaBot.getSilent().send("Вот ваше блюдо", update.getMessage().getChatId());
                            } catch(Exception e) {
                                System.out.println("Ошибка : " + e.getMessage());
                            }

                        },
                        isGetContext()
                )
                .build();
    }
    private Predicate<Update> isGetContext(){
        return update -> {
            boolean isGetContext = false;
            if (update.getMessage().getText().equals("/end")){
                return false;
            }
            try {
                final UserContextDTO userContextDTO = povaryoshkaBot.getDbDriver().selectUserContext(
                        new UserContextSelectOptions(
                                update.getMessage().getFrom().getId()
                        )
                );
                if (userContextDTO != null && userContextDTO.getMultiStateCommandTypes() == GET) {
                    isGetContext = true;
                }
            } catch(SQLException e) {
                System.out.println(e);
            }
            return isGetContext;

        };
    }
}