package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import static models.EnvVars.TELEGRAM_BOT_API_TOKEN;
import telegram.bot.PovaryoshkaBot;

public class Main {
    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(Main.class);
        logger.info("Povaryoshka bot started to work");
        String botToken = System.getenv(TELEGRAM_BOT_API_TOKEN);
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