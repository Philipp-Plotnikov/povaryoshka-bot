package language.ru;


public class BotMessages {
    public static final String WRITE_DISH_NAME = "Выбери название для блюда";
    public static final String WRITE_INGREDIENTS = "Перечисли ингредиенты";
    public static final String WRITE_RECIPE = "А теперь напиши рецепт";
    public static final String SOMETHING_WENT_WRONG = "Упс, кажется, что-то пошло не так. Попробуй, пожалуйста, ещё раз";
    public static final String DISH_WAS_CREATED_WITH_SUCCESS = "Блюдо успешно записано";
    public static final String USER_DOES_NOT_HAVE_DISHES = "У тебя нет сохраненных блюд";
    public static final String USER_DISHES_ARE = "Твои сохраненные блюда:";
    public static final String WRITE_DISH_NAME_FROM_LIST_TO_DELETE = "Выбери из списка название блюда, которое хочешь удалить";
    public static final String WRITE_DISH_NAME_FROM_LIST_TO_GET = "Выбери из списка название блюда, которое хочешь получить";
    public static final String WRITE_DISH_NAME_FROM_LIST_TO_UPDATE = "Выбери из списка название блюда, которое хочешь обновить";
    public static final String THIS_DISH_NAME_IS_NOT_FROM_LIST = "Такого блюда пока нет в списке. Проверь, что всё указано верно, и попробуй снова";
    public static final String DISH_WAS_DELETED_WITH_SUCCESS = "Блюдо успешно удалено";
    public static final String COMMAND_WAS_TERMINATED = "Команда прервана";
    public static final String WRITE_FEEDBACK = "Напиши свой отзыв о нашем сервисе. Нам будет приятно получить обратную связь :)";
    public static final String USER_FEEDBACK_WAS_SAVED = "Спасибо за обратную связь, мы обязательно её прочтем и постараемся стать еще лучше!";
    public static final String DISH_NAME = "Название";
    public static final String INGREDIENTS = "Ингредиенты";
    public static final String RECIPE = "Рецепт";
    public static final String NO_INFO = "Нет информации";
    public static final String USER_DISH_IS = "Вот блюдо, которое ты ищешь:";
    public static final String CONFIRM_INGREDIENTS_UPDATE = String.format(
        "Хочешь обновить ингредиенты блюда? (%s/%s)",
        UserMessages.YES,
        UserMessages.NO
    );
    public static final String  ENTER_YES_OR_NO= "Прости, я тебя не совсем понял, напиши «Да» или «Нет»";
    public static final String INGREDIENTS_ARE_NOT_UPDATED = "Хорошо, ингредиенты не меняем";
    public static final String INPUT_NEW_INGREDIENTS = "Перечисли новые ингредиенты";
    public static final String CONFIRM_RECIPE_UPDATE = String.format(
        "Хочешь обновить рецепт блюда? (%s/%s)",
        UserMessages.YES,
        UserMessages.NO
    );
    public static final String RECIPE_IS_NOT_UPDATED = "Хорошо, рецепт не меняем";
    public static final String INPUT_NEW_RECIPE = "Напиши новый рецепт";
    public static final String CONFIRM_DISH_NAME_UPDATE = String.format(
        "Хочешь обновить название блюда? (%s/%s)",
        UserMessages.YES,
        UserMessages.NO
    );
    public static final String DISH_NAME_IS_NOT_UPDATED = "Хорошо, название блюда не меняем";
    public static final String INPUT_NEW_DISH_NAME = "Напиши новое название блюда";
    public static final String DISH_WAS_UPDATED_WITH_SUCCESS = "Блюдо успешно обновлено";

}
