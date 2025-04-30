package core;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import telegram.bot.PovaryoshkaBot;

import static models.system.EnvVars.TELEGRAM_BOT_API_TOKEN;
import static utilities.CoreUtilities.getPovaryoshkaBot;

public class Application {
    @NonNull
    private final static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void start(){
        try (
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
        ) {
            logger.info("PovaryoshkaBot is launching");
            final PovaryoshkaBot povaryoshkaBot = getPovaryoshkaBot();
            final String botToken = System.getProperty(TELEGRAM_BOT_API_TOKEN);
            povaryoshkaBot.getDbDriver().setup();
            botsApplication.registerBot(botToken, povaryoshkaBot);
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error(String.valueOf(e));
        }
    }
}
