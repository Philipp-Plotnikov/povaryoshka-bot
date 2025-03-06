package core.factory;

import java.sql.SQLException;
import java.util.List;

import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import dbdrivers.IDbDriver;
import dbdrivers.factory.IDbDriverFactory;
import dbdrivers.factory.DbDriverFactoryProducer;
import models.commands.CommandTypes;
import models.db.DbTypes;
import telegram.bot.PovaryoshkaBot;
import telegram.abilities.factory.IAbilityFactory;
import telegram.abilities.factory.AbilityFactoryProducer;

import static models.system.EnvVars.*;

public class FacadeFactory {
    private final IDbDriverFactory dbDriverFactory;
    private final List<IAbilityFactory> abilityFactories;

    public FacadeFactory() throws Exception {
        final DbTypes dbType = DbTypes.valueOf(System.getenv(DB_TYPE).toUpperCase());
        final CommandTypes commandType = CommandTypes.valueOf(System.getenv(COMMAND_TYPE).toUpperCase());
        final CommandTypes reply = CommandTypes.valueOf(System.getenv(REPLY).toUpperCase());
        final DbDriverFactoryProducer dbDriverFactoryProducer = new DbDriverFactoryProducer();
        final AbilityFactoryProducer abilityFactoryProducer = new AbilityFactoryProducer();
        dbDriverFactory = dbDriverFactoryProducer.getDbDriverFactory(dbType);

        abilityFactories = List.of(
                abilityFactoryProducer.getAbilityFactory(commandType),
                abilityFactoryProducer.getAbilityFactory(reply)
        );
    }

    public IDbDriver getDbDriver() throws SQLException {
        return dbDriverFactory.getDbDriver();
    }

    public List<AbilityExtension> getCommandList(final PovaryoshkaBot povaryoshkaBot) {
        return abilityFactories.stream()
                .flatMap(factory -> factory.getAbilityList(povaryoshkaBot).stream())
                .toList();
    }
}
