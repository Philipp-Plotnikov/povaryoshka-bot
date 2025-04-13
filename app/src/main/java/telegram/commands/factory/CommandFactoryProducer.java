package telegram.commands.factory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import models.commands.ICommandFactoryGenerator;
import models.commands.CommandTypes;


final public class CommandFactoryProducer {
    @NonNull
    private final Map<@NonNull CommandTypes, @Nullable ICommandFactoryGenerator> commandFactoryGeneratorMap;

    public CommandFactoryProducer() {
        commandFactoryGeneratorMap = produceCommandFactoryGeneratorMap();
    }

    @NonNull
    private Map<@NonNull CommandTypes, @Nullable ICommandFactoryGenerator> produceCommandFactoryGeneratorMap() {
        final EnumMap<@NonNull CommandTypes, @Nullable ICommandFactoryGenerator> localCommandFactoryGeneratorMap = new EnumMap<>(CommandTypes.class);
        localCommandFactoryGeneratorMap.put(CommandTypes.SIMPLE, () -> new SimpleCommandFactory());
        return Collections.unmodifiableMap(localCommandFactoryGeneratorMap);
    }

    @NonNull
    public ICommandFactory produceCommandFactory(@NonNull final CommandTypes commandType) throws Exception {
        if (!commandFactoryGeneratorMap.containsKey(commandType)) {
            throw new Exception(String.format("%s was not found in commandFactoryGeneratorMap", commandType.name()));
        }
        final ICommandFactoryGenerator commandFactoryGenerator = commandFactoryGeneratorMap.get(commandType);
        if (commandFactoryGenerator == null) {
            throw new Exception(String.format("commandFactoryGenerator of commandType '%s' is null", commandType.name()));
        }
        return commandFactoryGenerator.generate();
    }
}