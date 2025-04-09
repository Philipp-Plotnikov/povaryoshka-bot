package core;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static models.system.EnvVars.BOT_USERNAME;
import static models.system.EnvVars.CREATOR_ID;
import static models.system.EnvVars.TELEGRAM_BOT_API_TOKEN;
import telegram.bot.PovaryoshkaBot;

public class Main {
    public static void main(String[] args) {
        try (
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
        ) {
            final String botToken = System.getenv(TELEGRAM_BOT_API_TOKEN);
            final String botUsername = System.getenv(BOT_USERNAME);
            final long creatorId = Long.parseLong(System.getenv(CREATOR_ID));
            final TelegramClient telegramClient = new OkHttpTelegramClient(botToken);
            final PovaryoshkaBot povaryoshkaBot = new PovaryoshkaBot(telegramClient, botUsername, creatorId);
            botsApplication.registerBot(botToken, povaryoshkaBot);
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}