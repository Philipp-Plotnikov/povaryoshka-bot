package telegram.bot;

import java.sql.SQLException;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import core.factory.FacadeFactory;
import dbdrivers.IDbDriver;

public class PovaryoshkaBot extends AbilityBot {
    private final long creatorId;

    @NonNull
    private final IDbDriver dbDriver;

    @NonNull
    private final FacadeFactory facadeFactory;

    public PovaryoshkaBot(
        @NonNull final TelegramClient telegramClient,
        @NonNull final String botUsername,
        final long creatorId
    ) throws SQLException, Exception {
        super(telegramClient, botUsername);
        this.creatorId = creatorId;
        facadeFactory = new FacadeFactory();
        dbDriver = facadeFactory.getDbDriver();
        dbDriver.setup();
    }

    public void initCommandList() {
        final List<AbilityExtension> commandList = facadeFactory.getCommandList(this);
        final List<AbilityExtension> replyList = facadeFactory.getReplyList(this);
        addExtensions(commandList);
        addExtensions(replyList);
        onRegister();
    }

    @NonNull
    public IDbDriver getDbDriver() {
        return dbDriver;
    }

    @Override
    public long creatorId() {
        return creatorId;
    }
}