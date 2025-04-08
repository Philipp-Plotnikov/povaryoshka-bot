package core.factory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import dbdrivers.IDbDriver;
import dbdrivers.factory.IDbDriverFactory;
import dbdrivers.factory.DbDriverFactoryProducer;

import static models.system.EnvVars.COMMAND_TYPE;
import static models.system.EnvVars.DB_TYPE;

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
        final DbTypes dbType = DbTypes.valueOf(System.getenv(DB_TYPE).toUpperCase());
        final CommandTypes commandType = CommandTypes.valueOf(System.getenv(COMMAND_TYPE).toUpperCase());
        final DbDriverFactoryProducer dbDriverFactoryProducer = new DbDriverFactoryProducer();
        final CommandFactoryProducer commandFactoryProducer = new CommandFactoryProducer();
        dbDriverFactory = dbDriverFactoryProducer.getDbDriverFactory(dbType);
        commandFactory = commandFactoryProducer.getCommandFactory(commandType);
        replyFactory = new ReplyFactory();
    }

    public IDbDriver getDbDriver() throws SQLException {
        return dbDriverFactory.getDbDriver();
    }

    public List<AbilityExtension> getCombinedExtensionsList(final PovaryoshkaBot povaryoshkaBot) {
        List<AbilityExtension> combinedList = new ArrayList<>();
        combinedList.addAll(commandFactory.getCommandList(povaryoshkaBot));
        combinedList.addAll(replyFactory.getReplyList(povaryoshkaBot));
        return combinedList;
    }

}