package core;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import static models.EnvVars.TELEGRAM_BOT_API_TOKEN;
import telegram.bot.PovaryoshkaBot;

public class Main {
    public static void main(String[] args) {
        try (
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()
        ) {
            String botToken = System.getenv(TELEGRAM_BOT_API_TOKEN);
            botsApplication.registerBot(botToken, new PovaryoshkaBot(botToken));
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}