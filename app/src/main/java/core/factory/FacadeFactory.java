package core.factory;

import java.sql.SQLException;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import dbdrivers.IDbDriver;
import dbdrivers.factory.IDbDriverFactory;
import dbdrivers.factory.DbDriverFactoryProducer;

import static utilities.CommonsUtilities.getDbType;
import static utilities.CoreUtilities.getCommandType;
import models.commands.CommandTypes;
import models.db.DbTypes;
import telegram.bot.PovaryoshkaBot;
import telegram.commands.factory.ICommandFactory;
import telegram.commands.factory.CommandFactoryProducer;
import telegram.replies.factory.IReplyFactory;
import telegram.replies.factory.ReplyFactory;


public class FacadeFactory {
    private final IDbDriverFactory dbDriverFactory;
    private final ICommandFactory commandFactory;
    private final IReplyFactory replyFactory;

    public FacadeFactory() throws Exception {
        final CommandTypes commandType = getCommandType();
        final DbTypes dbType = getDbType();
        final DbDriverFactoryProducer dbDriverFactoryProducer = new DbDriverFactoryProducer();
        final CommandFactoryProducer commandFactoryProducer = new CommandFactoryProducer();
        dbDriverFactory = dbDriverFactoryProducer.getDbDriverFactory(dbType);
        commandFactory = commandFactoryProducer.getCommandFactory(commandType);
        replyFactory = new ReplyFactory();
    }

    public IDbDriver getDbDriver() throws SQLException {
        return dbDriverFactory.getDbDriver();
    }

    @NonNull
    public Map<String, @Nullable AbilityExtension> createCommandMap(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        return commandFactory.createCommandMap(povaryoshkaBot);
    }

    @NonNull
    public Map<String, @Nullable AbilityExtension> createReplyMap(final PovaryoshkaBot povaryoshkaBot) {
        return replyFactory.createReplyMap(povaryoshkaBot);
    }
}