package telegram.bot;

import java.sql.SQLException;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import dbdrivers.DbDriver;
import dbdrivers.postgres.PostgresDbDriver;
import static models.EnvVars.ALTER_SQL_SCRIPT_PATH;
import static models.EnvVars.DB_DATABASE;
import static models.EnvVars.DB_HOST;
import static models.EnvVars.DB_PASSWORD;
import static models.EnvVars.DB_PORT;
import static models.EnvVars.DB_SCHEMA;
import static models.EnvVars.DB_USERNAME;
import static models.EnvVars.INIT_SQL_SCRIPT_PATH;
import models.dbdrivers.postgres.PostgresDbDriverOptions;

public class PovaryoshkaBot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final DbDriver dbDriver;

    public PovaryoshkaBot(String botToken) throws SQLException, Exception {
        telegramClient = new OkHttpTelegramClient(botToken);
        final PostgresDbDriverOptions postgresDbDriverOptions = new PostgresDbDriverOptions(
            System.getenv(DB_HOST),
            System.getenv(DB_PORT),
            System.getenv(DB_DATABASE),
            System.getenv(DB_SCHEMA),
            System.getenv(DB_USERNAME),
            System.getenv(DB_PASSWORD),
            System.getenv(INIT_SQL_SCRIPT_PATH),
            System.getenv(ALTER_SQL_SCRIPT_PATH)
        );
        dbDriver = PostgresDbDriver.getInstance(postgresDbDriverOptions);
        dbDriver.connect();
        dbDriver.setup();
    }

    @Override
    public void consume(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            SendMessage message = SendMessage // Create a message object
                    .builder()
                    .chatId(chat_id)
                    .text(message_text)
                    .build();
            try {
                telegramClient.execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}