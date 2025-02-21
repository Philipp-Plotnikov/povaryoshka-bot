package core;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import static models.system.EnvVars.TELEGRAM_BOT_API_TOKEN;
import static utilities.CoreUtilities.getPovaryoshkaBot;

import telegram.bot.PovaryoshkaBot;


public class Main {
    public static void main(String[] args) {
        try (
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
        ) {
            final PovaryoshkaBot povaryoshkaBot = getPovaryoshkaBot();
            final String botToken = System.getenv(TELEGRAM_BOT_API_TOKEN);
            povaryoshkaBot.initCommandList();
            botsApplication.registerBot(botToken, povaryoshkaBot);
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}