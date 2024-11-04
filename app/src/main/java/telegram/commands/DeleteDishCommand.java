package telegram.commands;

import models.commands.CommandStates;
import models.db.sqlops.dish.DishDeleteOptions;
import models.db.sqlops.dish.DishListSelectOptions;
import models.db.sqlops.feedback.FeedbackInsertOptions;
import models.db.sqlops.usercontext.UserContextDeleteOptions;
import models.db.sqlops.usercontext.UserContextInsertOptions;
import models.db.sqlops.usercontext.UserContextSelectOptions;
import models.dtos.DishDTO;
import models.dtos.UserContextDTO;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import static models.commands.CommandStates.DISH_NAME;
import static models.commands.MultiStateCommandTypes.DELETE;
import static models.commands.MultiStateCommandTypes.FEEDBACK;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import static models.commands.CommandConfig.DELETE_DISH_COMMAND_SETTINGS;

import org.telegram.telegrambots.meta.api.objects.Update;
import telegram.bot.PovaryoshkaBot;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Predicate;

public class DeleteDishCommand implements AbilityExtension {
    @NonNull
    private final PovaryoshkaBot povaryoshkaBot;

    public DeleteDishCommand(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        this.povaryoshkaBot = povaryoshkaBot;
    }

    @NonNull
    public Ability deleteDish() {
        return Ability.builder()
            .name(DELETE_DISH_COMMAND_SETTINGS.commandName())
            .info(DELETE_DISH_COMMAND_SETTINGS.commandDescription())
            .privacy(PUBLIC)
            .locality(ALL) // ?
            .action(ctx -> {
                try {
                    povaryoshkaBot.getDbDriver().insertUserContext(
                            new UserContextInsertOptions(
                                    ctx.user().getId(),
                                    DELETE,
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
                    povaryoshkaBot.getSilent().send("Напишите название блюда из списка, которое хотите удалить", ctx.chatId());

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
                                if (userContextDTO.getMultiStateCommandTypes() == DELETE) {
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
                                        povaryoshkaBot.getDbDriver().deleteDish(new DishDeleteOptions(
                                                update.getMessage().getFrom().getId(),
                                                dishName
                                        ));
                                        povaryoshkaBot.getDbDriver().deleteUserContext(
                                                new UserContextDeleteOptions(
                                                        update.getMessage().getFrom().getId()
                                                )
                                        );
                                        povaryoshkaBot.getSilent().send("Блюдо удалено", update.getMessage().getChatId());
                                    } else {
                                        povaryoshkaBot.getSilent().send("Такого блюда нет в списке. Попробуйте снова.", update.getMessage().getChatId());
                                    }
                                }
                            } catch(Exception e) {
                                System.out.println("Ошибка : " + e.getMessage());
                            }

                        },
                        isDeleteContext()
                )
            .build();
    }
    private Predicate<Update> isDeleteContext(){
        return update -> {
            boolean isDeleteContext = false;
            try {
                final UserContextDTO userContextDTO = povaryoshkaBot.getDbDriver().selectUserContext(
                        new UserContextSelectOptions(
                                update.getMessage().getFrom().getId()
                        )
                );
                if (userContextDTO.getMultiStateCommandTypes() == DELETE) {
                    isDeleteContext = true;
                }
            } catch(SQLException e) {
                System.out.println(e);
            }
            return isDeleteContext;

        };
    }
}