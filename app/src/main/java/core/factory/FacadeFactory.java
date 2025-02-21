package core.factory;

import java.sql.SQLException;
import java.util.List;

import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import dbdrivers.DbDriver;
import dbdrivers.factory.DbDriverFactory;
import dbdrivers.factory.DbDriverFactoryProducer;
import static models.system.EnvVars.COMMAND_TYPE;
import static models.system.EnvVars.DB_TYPE;
import models.commands.CommandTypes;
import models.db.DbTypes;
import telegram.bot.PovaryoshkaBot;
import telegram.commands.factory.CommandFactory;
import telegram.commands.factory.CommandFactoryProducer;

public class FacadeFactory {
    private final DbDriverFactory dbDriverFactory;
    private final CommandFactory commandFactory;

    public FacadeFactory() throws Exception {
        final DbTypes dbType = DbTypes.valueOf(System.getenv(DB_TYPE).toUpperCase());
        final CommandTypes commandType = CommandTypes.valueOf(System.getenv(COMMAND_TYPE).toUpperCase());
        final DbDriverFactoryProducer dbDriverFactoryProducer = new DbDriverFactoryProducer();
        final CommandFactoryProducer commandFactoryProducer = new CommandFactoryProducer();
        dbDriverFactory = dbDriverFactoryProducer.getDbDriverFactory(dbType);
        commandFactory = commandFactoryProducer.getCommandFactory(commandType);
    }

    public DbDriver getDbDriver() throws SQLException {
        return dbDriverFactory.getDbDriver();
    }

    public List<AbilityExtension> getCommandList(final PovaryoshkaBot povaryoshkaBot) {
        return commandFactory.getCommandList(povaryoshkaBot);
    }
}