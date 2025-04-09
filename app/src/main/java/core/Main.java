package core;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import static utilities.CoreUtilities.getPovaryoshkaBot;
import static utilities.CoreUtilities.loadEnvFileToSystemProperties;
import static models.system.EnvVars.TELEGRAM_BOT_API_TOKEN;
import telegram.bot.PovaryoshkaBot;


public class Main {
    public static void main(String[] args) {
        loadEnvFileToSystemProperties();
        try (
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
        ) {
            final PovaryoshkaBot povaryoshkaBot = getPovaryoshkaBot();
            final String botToken = System.getProperty(TELEGRAM_BOT_API_TOKEN);
            povaryoshkaBot.getDbDriver().setup();
            botsApplication.registerBot(botToken, povaryoshkaBot);
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}