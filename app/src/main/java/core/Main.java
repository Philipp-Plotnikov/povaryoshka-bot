package core;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import static utilities.CoreUtilities.getPovaryoshkaBot;
import static utilities.CoreUtilities.loadEnvFileToSystemProperties;
import static models.system.EnvVars.TELEGRAM_BOT_API_TOKEN;
import telegram.bot.PovaryoshkaBot;


public class Main {

    public static void main(String[] args) {
        loadEnvFileToSystemProperties();
        @NonNull final Logger logger = LoggerFactory.getLogger(Main.class);
        try (
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
        ) {
            final PovaryoshkaBot povaryoshkaBot = getPovaryoshkaBot();
            final String botToken = System.getProperty(TELEGRAM_BOT_API_TOKEN);
            povaryoshkaBot.getDbDriver().setup();
            botsApplication.registerBot(botToken, povaryoshkaBot);
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}