package core.factory;

import dbdrivers.IDbDriver;
import dbdrivers.factory.DbDriverFactoryProducer;
import dbdrivers.factory.IDbDriverFactory;
import models.commands.CommandTypes;
import models.db.DbTypes;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import telegram.bot.PovaryoshkaBot;
import telegram.commands.factory.CommandFactoryProducer;
import telegram.commands.factory.ICommandFactory;
import telegram.replies.factory.IReplyFactory;
import telegram.replies.factory.ReplyFactory;

import java.sql.SQLException;
import java.util.Map;

import static utilities.CommonsUtilities.getDbType;
import static utilities.CoreUtilities.getCommandType;


final public class FacadeFactory {
    private final IDbDriverFactory dbDriverFactory;
    private final ICommandFactory commandFactory;
    private final IReplyFactory replyFactory;

    @NonNull
    private final Logger logger = LoggerFactory.getLogger(FacadeFactory.class);

    public FacadeFactory() throws Exception {
        final CommandTypes commandType = getCommandType();
        final DbTypes dbType = getDbType();
        final DbDriverFactoryProducer dbDriverFactoryProducer = new DbDriverFactoryProducer();
        final CommandFactoryProducer commandFactoryProducer = new CommandFactoryProducer();
        dbDriverFactory = dbDriverFactoryProducer.produceDbDriverFactory(dbType);
        commandFactory = commandFactoryProducer.produceCommandFactory(commandType);
        replyFactory = new ReplyFactory();
        logger.debug("FacadeFactory was initialized");
    }

    public IDbDriver getDbDriver() throws SQLException {
        return dbDriverFactory.createDbDriver();
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