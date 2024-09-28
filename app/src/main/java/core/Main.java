package core;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import models.EnvVars;
import telegram.bot.PovaryoshkaBot;

public class Main {

    public static void main(String[] args) {
        String botToken = System.getenv("TELEGRAM_BOT_API_TOKEN");
        var a = EnvVars.valueOf("Test");
        try (
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()
        ) {
            botsApplication.registerBot(botToken, new PovaryoshkaBot(botToken));
            System.out.println("PovaryoshkaBot successfully started!");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}