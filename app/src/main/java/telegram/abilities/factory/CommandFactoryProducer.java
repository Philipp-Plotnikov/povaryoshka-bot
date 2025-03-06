package telegram.abilities.factory;

import models.commands.CommandTypes;
import models.commands.IAbilityFactoryGenerator;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class CommandFactoryProducer {
    @NonNull
    private final Map<@NonNull CommandTypes, @Nullable IAbilityFactoryGenerator> abilityFactoryGeneratorMap;

    public CommandFactoryProducer() {
        abilityFactoryGeneratorMap = getAbilityFactoryGeneratorMap();
    }

    @NonNull
    private Map<@NonNull CommandTypes, @Nullable IAbilityFactoryGenerator> getAbilityFactoryGeneratorMap() {
        final EnumMap<@NonNull CommandTypes, @Nullable IAbilityFactoryGenerator> localCommandFactoryGeneratorMap = new EnumMap<>(CommandTypes.class);
        localCommandFactoryGeneratorMap.put(CommandTypes.SIMPLE, SimpleCommandFactory::new);
        localCommandFactoryGeneratorMap.put(CommandTypes.REPLY, SimpleReplyFactory::new);
        return Collections.unmodifiableMap(localCommandFactoryGeneratorMap);
    }

    @NonNull
    public IAbilityFactory getCommandFactory(@NonNull final CommandTypes commandType) throws Exception {
        if (!abilityFactoryGeneratorMap.containsKey(commandType)) {
            throw new Exception(String.format("%s was not found in commandFactoryGeneratorMap", commandType.name()));
        }
        final IAbilityFactoryGenerator commandFactoryGenerator = abilityFactoryGeneratorMap.get(commandType);
        if (commandFactoryGenerator == null) {
            throw new Exception(String.format("commandFactoryGenerator of commandType '%s' is null", commandType.name()));
        }
        return commandFactoryGenerator.generate();
    }
}