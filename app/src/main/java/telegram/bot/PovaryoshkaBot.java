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
        commandMap = facadeFactory.getCommandMap(this);
        addExtensions(commandMap.values());
        onRegister();
    }

    @NonNull
    public IDbDriver getDbDriver() {
        return dbDriver;
    }

    @Nullable
    public Map<String, @Nullable AbilityExtension> getCommandMap() {
        return commandMap;
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    public void setSilentSender(@NonNull SilentSender newSilentSender) {
        silent = newSilentSender;
    }
}