package telegram.bot;

import java.sql.SQLException;
import java.util.List;

import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import core.factory.FacadeFactory;
import dbdrivers.DbDriver;

// TODO: Think about command list
public class PovaryoshkaBot extends AbilityBot {
    private final long creatorId;
    private final DbDriver dbDriver;
    private final FacadeFactory facadeFactory;

    // TODO: Use addExtensions
    // TODO: Read about methods in Java
    public PovaryoshkaBot(
        final TelegramClient telegramClient,
        final String botUsername,
        final long creatorId
    ) throws SQLException, Exception {
        super(telegramClient, botUsername);
        this.creatorId = creatorId;
        facadeFactory = new FacadeFactory();
        dbDriver = facadeFactory.getDbDriver();
        dbDriver.connect();
        dbDriver.setup();
    }

    // TODO: Think about it
    public void initCommandList() {
        final List<AbilityExtension> commandList = facadeFactory.getCommandList(this);
        addExtensions(commandList);
        onRegister();
    }

    public DbDriver getDbDriver() {
        return dbDriver;
    }

    @Override
    public long creatorId() {
        return creatorId;
    }
}