package telegram.bot;

import java.util.ArrayList;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import dbdrivers.AbstractDbDriver;
import dbdrivers.postgres.PostgresDbDriver;
import models.EnvVars;
import models.dbdrivers.postgres.PostgresDbDriverOptions;
import models.sqlops.dish.DishDeleteOptions;
import models.sqlops.dish.DishInsertOptions;
import models.sqlops.dish.DishSelectOptions;
import models.sqlops.dish.DishUpdateOptions;

public class PovaryoshkaBot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final AbstractDbDriver dbDriver;

    public PovaryoshkaBot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        final PostgresDbDriverOptions postgresDbDriverOptions = new PostgresDbDriverOptions(
            System.getenv(EnvVars.DB_HOST.getValue()),
            System.getenv(EnvVars.DB_PORT.getValue()),
            System.getenv(EnvVars.DB_DATABASE.getValue()),
            System.getenv(EnvVars.DB_SCHEMA.getValue()),
            System.getenv(EnvVars.DB_USERNAME.getValue()),
            System.getenv(EnvVars.DB_PASSWORD.getValue())
        );
        this.dbDriver = new PostgresDbDriver(postgresDbDriverOptions);
    }

    @Override
    public void consume(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            // TODO: delete
            final ArrayList<String> ingredientList = new ArrayList<>();
            ingredientList.add("cucumber");

            try {
                if (message_text.contains("testDB")) {
                    this.dbDriver.connect();
                    this.dbDriver.setup();
                }
                if (message_text.contains("insertDB")) {
                    this.dbDriver.insertDish(
                        new DishInsertOptions(
                            update.getMessage().getFrom().getId(),
                            "test-dish",
                            ingredientList,
                            "do something"
                        )
                    );
                }
                if (message_text.contains("deleteDB")) {
                    this.dbDriver.deleteDish(
                        new DishDeleteOptions(
                            update.getMessage().getFrom().getId(),
                            "test-dish"
                        )
                    );
                }
                if (message_text.contains("updateDB")) {
                    this.dbDriver.updateDish(
                        new DishUpdateOptions(
                            update.getMessage().getFrom().getId(),
                            "test-dish",
                            ingredientList,
                            "updated do something"
                        )
                    );
                }
                if (message_text.contains("selectDB")) {
                    this.dbDriver.selectDish(
                        new DishSelectOptions(
                            update.getMessage().getFrom().getId(),
                            "test-dish"
                        )
                    );
                }
            } catch (Exception e) {
                System.out.println(e);
            }

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