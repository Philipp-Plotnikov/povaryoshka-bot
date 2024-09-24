package org.telegram.bot;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class PovaryoshkaBot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;

    public PovaryoshkaBot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
    }

    public ReplyKeyboardMarkup DrawKeyboard(String[]... rows) { // <тип данных>... <имя переменной> -- неограниченное исло параметров
            
            // создаем клавиатуру, и строку для нее
            List<KeyboardRow> keyboard = new ArrayList<>();
            KeyboardRow kRow;

            // проходимся по массиву массивов
            for (String[] row : rows) {                         // берем 1 массив
                kRow = new KeyboardRow();                       // создаем строку кнопок
                for (String buttonName : row) {                 // берем из массива строку-название_кнопки
                    kRow.add(new KeyboardButton(buttonName));   // кладем ее в строку кнопок
                }
                keyboard.add(kRow);                             // добавляем строку кнопок в клавиатуру
            }

            // Добавляем строку с кнопками в клавиатуру
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(keyboard);

            // Устанавливаем клавиатуру для сообщения
            keyboardMarkup.setResizeKeyboard(true); // Клавиатура будет изменять размер в зависимости от экрана
            keyboardMarkup.setOneTimeKeyboard(true); // Клавиатура будет скрыта после использования (при необходимости)

            return keyboardMarkup;
    }
    
    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {            
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // newMessage, *buttons = Handler(messageText);

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(messageText)
                    .build();

            // Присваиваем клавиатуру сообщению
            // временная затычка, пока хендлер ничего не возвращает, далее передаем buttons
            String[] row1 = {"r1k1", "r1k2", "r1k3"};
            String[] row2 = {"r2k1", "r2k2", "r2k3"};
            message.setReplyMarkup(DrawKeyboard(row1, row2));

            // Отправляем сообщение пользователю
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace(); // Логируем ошибку, если отправка не удалась
            }
        }
    }
}