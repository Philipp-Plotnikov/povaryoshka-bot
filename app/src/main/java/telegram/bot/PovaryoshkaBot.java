package telegram.bot;

import java.sql.SQLException;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;
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

    @Nullable
    private Map<String, @Nullable AbilityExtension> commandMap;

    @Nullable
    private Map<String, @Nullable AbilityExtension> replyMap;

    public PovaryoshkaBot(
        @NonNull final TelegramClient telegramClient,
        @NonNull final String botUsername,
        final long creatorId
    ) throws SQLException, Exception {
        super(telegramClient, botUsername);
        this.creatorId = creatorId;
        facadeFactory = new FacadeFactory();
        dbDriver = facadeFactory.getDbDriver();
        initCommandList();
    }

    public void initCommandList() {
        commandMap = facadeFactory.createCommandMap(this);
        replyMap = facadeFactory.createReplyMap(this);
        addExtensions(commandMap.values());
        addExtensions(replyMap.values());
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

    @Nullable
    public Map<String, @Nullable AbilityExtension> getCommandMap() {
        return commandMap;
    }

    @Nullable
    public Map<String, @Nullable AbilityExtension> getReplyMap() {
        return replyMap;
    }

    public void setSilentSender(@NonNull SilentSender newSilentSender) {
        silent = newSilentSender;
    }
}