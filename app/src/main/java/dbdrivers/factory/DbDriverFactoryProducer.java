package dbdrivers.factory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import models.db.DbTypes;
import models.db.drivers.IDbDriverFactoryGenerator;


final public class DbDriverFactoryProducer {
    @NonNull private final Map<@NonNull DbTypes, @Nullable IDbDriverFactoryGenerator> dbDriverFactoryGeneratorMap;

    public DbDriverFactoryProducer() {
        dbDriverFactoryGeneratorMap = produceDbDriverFactoryGeneratorMap();
    }

    @NonNull
    private Map<@NonNull DbTypes, @Nullable IDbDriverFactoryGenerator> produceDbDriverFactoryGeneratorMap() {
        final EnumMap<@NonNull DbTypes, @Nullable IDbDriverFactoryGenerator> localDbDriverFactoryGeneratorMap = new EnumMap<>(DbTypes.class);
        localDbDriverFactoryGeneratorMap.put(DbTypes.POSTGRES, () -> new PostgresDbDriverFactory());
        return Collections.unmodifiableMap(localDbDriverFactoryGeneratorMap);
    }

    @NonNull
    public IDbDriverFactory produceDbDriverFactory(@NonNull final DbTypes dbType) throws Exception {
        if (!dbDriverFactoryGeneratorMap.containsKey(dbType)) {
            throw new Exception(String.format("dbType '%s' was not found in dbDriverFactoryGeneratorMap", dbType.name()));
        }
        final IDbDriverFactoryGenerator dbDriverFactoryGenerator = dbDriverFactoryGeneratorMap.get(dbType);
        if (dbDriverFactoryGenerator == null) {
            throw new Exception(String.format("dbDriverFactoryGenerator of dbType '%s' is null", dbType.name()));
        }
        return dbDriverFactoryGenerator.generate();
    }
}