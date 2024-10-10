package telegram.commands.factory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import models.commands.CommandFactoryGenerator;
import models.commands.CommandTypes;

public class CommandFactoryProducer {
    private final Map<CommandTypes, CommandFactoryGenerator> commandFactoryGeneratorMap;

    public CommandFactoryProducer() {
        commandFactoryGeneratorMap = getCommandFactoryGeneratorMap();
    }

    private Map<CommandTypes, CommandFactoryGenerator> getCommandFactoryGeneratorMap() {
        final EnumMap<CommandTypes, CommandFactoryGenerator> localCommandFactoryGeneratorMap = new EnumMap<>(CommandTypes.class);
        localCommandFactoryGeneratorMap.put(CommandTypes.SIMPLE, () -> new SimpleCommandFactory());
        return Collections.unmodifiableMap(localCommandFactoryGeneratorMap);
    }

    public CommandFactory getCommandFactory(final CommandTypes commandType) throws Exception {
        if (!commandFactoryGeneratorMap.containsKey(commandType)) {
            throw new Exception(String.format("%s was not found in commandFactoryGeneratorMap", commandType.name()));
        }
        return commandFactoryGeneratorMap.get(commandType).generate();
    }
}