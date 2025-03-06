package telegram.abilities.factory;

import models.commands.CommandTypes;
import models.commands.IAbilityFactoryGenerator;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class AbilityFactoryProducer {
    @NonNull
    private final Map<@NonNull CommandTypes, @Nullable IAbilityFactoryGenerator> abilityFactoryGeneratorMap;

    public AbilityFactoryProducer() {
        abilityFactoryGeneratorMap = getAbilityFactoryGeneratorMap();
    }

    @NonNull
    private Map<@NonNull CommandTypes, @Nullable IAbilityFactoryGenerator> getAbilityFactoryGeneratorMap() {
        final EnumMap<@NonNull CommandTypes, @Nullable IAbilityFactoryGenerator> localAbilityFactoryGeneratorMap = new EnumMap<>(CommandTypes.class);
        localAbilityFactoryGeneratorMap.put(CommandTypes.SIMPLE, SimpleCommandFactory::new);
        localAbilityFactoryGeneratorMap.put(CommandTypes.REPLY, SimpleReplyFactory::new);
        return Collections.unmodifiableMap(localAbilityFactoryGeneratorMap);
    }

    @NonNull
    public IAbilityFactory getAbilityFactory(@NonNull final CommandTypes commandType) throws Exception {
        if (!abilityFactoryGeneratorMap.containsKey(commandType)) {
            throw new Exception(String.format("%s was not found in commandFactoryGeneratorMap", commandType.name()));
        }
        final IAbilityFactoryGenerator abilityFactoryGenerator = abilityFactoryGeneratorMap.get(commandType);
        if (abilityFactoryGenerator == null) {
            throw new Exception(String.format("commandFactoryGenerator of commandType '%s' is null", commandType.name()));
        }
        return abilityFactoryGenerator.generate();
    }
}