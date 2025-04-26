package utilities;

import java.sql.SQLException;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import io.github.cdimascio.dotenv.Dotenv;
import models.commands.CommandTypes;

import static models.system.EnvVars.BOT_USERNAME;
import static models.system.EnvVars.CREATOR_ID;
import static models.system.EnvVars.TELEGRAM_BOT_API_TOKEN;
import static models.system.EnvVars.COMMAND_TYPE;

import telegram.bot.PovaryoshkaBot;


final public class CoreUtilities {
    public static void loadEnvFileToSystemProperties() {
        Dotenv dotenv = Dotenv.configure().systemProperties().load();
        System.setProperty("MAX_BUFFER_COUNT", dotenv.get("MAX_BUFFER_COUNT"));
        System.setProperty("FLUSH_INTERVAL_SEC", dotenv.get("FLUSH_INTERVAL_SEC"));
    }

    @NonNull
    public static PovaryoshkaBot getPovaryoshkaBot() throws SQLException, Exception {
        final String botToken = System.getProperty(TELEGRAM_BOT_API_TOKEN);
        final String botUsername = System.getProperty(BOT_USERNAME);
        final long creatorId = Long.parseLong(System.getProperty(CREATOR_ID));
        final TelegramClient telegramClient = new OkHttpTelegramClient(botToken);
        return new PovaryoshkaBot(telegramClient, botUsername, creatorId);
    }

    @NonNull
    public static CommandTypes getCommandType() {
        final String commandType = System.getProperty(COMMAND_TYPE).toUpperCase();
        return CommandTypes.valueOf(commandType);
    }
}
