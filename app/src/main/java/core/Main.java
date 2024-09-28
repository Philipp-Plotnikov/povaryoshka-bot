package core;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import telegram.bot.PovaryoshkaBot;

public class Main {

    public static void main(String[] args) {
        String botToken = System.getenv("TELEGRAM_BOT_API_TOKEN");
        try (
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()
        ) {
            botsApplication.registerBot(botToken, new PovaryoshkaBot(botToken));
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}