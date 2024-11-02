package telegram.commands.factory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import models.commands.CommandFactoryGenerator;
import models.commands.CommandTypes;

public class CommandFactoryProducer {
    @NonNull
    private final Map<@NonNull CommandTypes, @Nullable CommandFactoryGenerator> commandFactoryGeneratorMap;

    public CommandFactoryProducer() {
        commandFactoryGeneratorMap = getCommandFactoryGeneratorMap();
    }

    @NonNull
    private Map<@NonNull CommandTypes, @Nullable CommandFactoryGenerator> getCommandFactoryGeneratorMap() {
        final EnumMap<@NonNull CommandTypes, @Nullable CommandFactoryGenerator> localCommandFactoryGeneratorMap = new EnumMap<>(CommandTypes.class);
        localCommandFactoryGeneratorMap.put(CommandTypes.SIMPLE, () -> new SimpleCommandFactory());
        return Collections.unmodifiableMap(localCommandFactoryGeneratorMap);
    }

    @NonNull
    public CommandFactory getCommandFactory(@NonNull final CommandTypes commandType) throws Exception {
        if (!commandFactoryGeneratorMap.containsKey(commandType)) {
            throw new Exception(String.format("%s was not found in commandFactoryGeneratorMap", commandType.name()));
        }
        final CommandFactoryGenerator commandFactoryGenerator = commandFactoryGeneratorMap.get(commandType);
        if (commandFactoryGenerator == null) {
            throw new Exception(String.format("commandFactoryGenerator of commandType '%s' is null", commandType.name()));
        }
        return commandFactoryGenerator.generate();
    }
}