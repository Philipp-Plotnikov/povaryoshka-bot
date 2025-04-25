package dbdrivers.factory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import models.db.DbTypes;
import models.db.drivers.IDbDriverFactoryGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final public class DbDriverFactoryProducer {
    @NonNull private final Map<@NonNull DbTypes, @Nullable IDbDriverFactoryGenerator> dbDriverFactoryGeneratorMap;

    @NonNull private final Logger logger = LoggerFactory.getLogger(DbDriverFactoryProducer.class);

    public DbDriverFactoryProducer() {
        dbDriverFactoryGeneratorMap = produceDbDriverFactoryGeneratorMap();
    }

    @NonNull
    private Map<@NonNull DbTypes, @Nullable IDbDriverFactoryGenerator> produceDbDriverFactoryGeneratorMap() {
        final EnumMap<@NonNull DbTypes, @Nullable IDbDriverFactoryGenerator> localDbDriverFactoryGeneratorMap = new EnumMap<>(DbTypes.class);
        localDbDriverFactoryGeneratorMap.put(DbTypes.POSTGRES, () -> new PostgresDbDriverFactory());
        logger.debug("DbDriverFactoryGeneratorMap has been initialized");
        return Collections.unmodifiableMap(localDbDriverFactoryGeneratorMap);
    }

    @NonNull
    public IDbDriverFactory produceDbDriverFactory(@NonNull final DbTypes dbType) throws Exception {
        if (!dbDriverFactoryGeneratorMap.containsKey(dbType)) {
            logger.error("dbType {} was not found in dbDriverFactoryGeneratorMap", dbType.name());
            throw new Exception(String.format("dbType '%s' was not found in dbDriverFactoryGeneratorMap", dbType.name()));
        }
        final IDbDriverFactoryGenerator dbDriverFactoryGenerator = dbDriverFactoryGeneratorMap.get(dbType);
        if (dbDriverFactoryGenerator == null) {
            logger.error("dbDriverFactoryGenerator of dbType {} is null", dbType.name());
            throw new Exception(String.format("dbDriverFactoryGenerator of dbType '%s' is null", dbType.name()));
        }
        logger.debug("Got DbDriverFactory");
        return dbDriverFactoryGenerator.generate();
    }
}