package telegram.commands.factory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import models.commands.ICommandFactoryGenerator;
import models.commands.CommandTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telegram.replies.DefaultReply;


final public class CommandFactoryProducer {
    @NonNull
    private final static Logger logger = LoggerFactory.getLogger(CommandFactoryProducer.class);

    @NonNull
    private final Map<@NonNull CommandTypes, @Nullable ICommandFactoryGenerator> commandFactoryGeneratorMap;

    public CommandFactoryProducer() {
        commandFactoryGeneratorMap = produceCommandFactoryGeneratorMap();
    }

    @NonNull
    private Map<@NonNull CommandTypes, @Nullable ICommandFactoryGenerator> produceCommandFactoryGeneratorMap() {
        final EnumMap<@NonNull CommandTypes, @Nullable ICommandFactoryGenerator> localCommandFactoryGeneratorMap = new EnumMap<>(CommandTypes.class);
        localCommandFactoryGeneratorMap.put(CommandTypes.SIMPLE, () -> new SimpleCommandFactory());
        logger.debug("Created CommandFactoryGeneratorMap");
        return Collections.unmodifiableMap(localCommandFactoryGeneratorMap);
    }

    @NonNull
    public ICommandFactory produceCommandFactory(@NonNull final CommandTypes commandType) throws Exception {
        if (!commandFactoryGeneratorMap.containsKey(commandType)) {
            logger.error("{} was not found in commandFactoryGeneratorMap", commandType.name());
            throw new Exception(String.format("%s was not found in commandFactoryGeneratorMap", commandType.name()));
        }
        final ICommandFactoryGenerator commandFactoryGenerator = commandFactoryGeneratorMap.get(commandType);
        if (commandFactoryGenerator == null) {
            logger.error("commandFactoryGenerator of commandType {} is null", commandType.name());
            throw new Exception(String.format("commandFactoryGenerator of commandType '%s' is null", commandType.name()));
        }
        logger.debug("Created CommandFactory");
        return commandFactoryGenerator.generate();
    }
}