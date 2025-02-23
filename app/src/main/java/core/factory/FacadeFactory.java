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


public class FacadeFactory {
    private final IDbDriverFactory dbDriverFactory;
    private final ICommandFactory commandFactory;

    public FacadeFactory() throws Exception {
        final CommandTypes commandType = getCommandType();
        final DbTypes dbType = getDbType();
        final CommandFactoryProducer commandFactoryProducer = new CommandFactoryProducer();
        final DbDriverFactoryProducer dbDriverFactoryProducer = new DbDriverFactoryProducer();
        commandFactory = commandFactoryProducer.getCommandFactory(commandType);
        dbDriverFactory = dbDriverFactoryProducer.getDbDriverFactory(dbType);
    }

    public IDbDriver getDbDriver() throws SQLException {
        return dbDriverFactory.getDbDriver();
    }

    @NonNull
    public Map<String, @Nullable AbilityExtension> getCommandMap(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        return commandFactory.getCommandMap(povaryoshkaBot);
    }
}