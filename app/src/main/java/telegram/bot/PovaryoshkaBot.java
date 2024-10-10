package telegram.bot;

import java.sql.SQLException;
import java.util.Map;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import core.factory.FacadeFactory;
import dbdrivers.DbDriver;
import telegram.commands.AbstractCommand;

public class PovaryoshkaBot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final DbDriver dbDriver;
    private final Map<String, AbstractCommand> commandMap;

    public PovaryoshkaBot(String botToken) throws SQLException, Exception {
        telegramClient = new OkHttpTelegramClient(botToken);
        final FacadeFactory facadeFactory = new FacadeFactory();
        commandMap = facadeFactory.getCommandMap();
        dbDriver = facadeFactory.getDbDriver();
        dbDriver.connect();
        dbDriver.setup();
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            SendMessage message = SendMessage
                    .builder()
                    .chatId(chat_id)
                    .text(message_text)
                    .build();
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}