package dbdrivers.factory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import models.db.DbTypes;
import models.db.drivers.DbDriverFactoryGenerator;


public class DbDriverFactoryProducer {
    @NonNull private final Map<@NonNull DbTypes, @Nullable DbDriverFactoryGenerator> dbDriverFactoryGeneratorMap;

    public DbDriverFactoryProducer() {
        dbDriverFactoryGeneratorMap = getDbDriverFactoryGeneratorMap();
    }

    @NonNull
    private Map<@NonNull DbTypes, @Nullable DbDriverFactoryGenerator> getDbDriverFactoryGeneratorMap() {
        final EnumMap<@NonNull DbTypes, @Nullable DbDriverFactoryGenerator> localDbDriverFactoryGeneratorMap = new EnumMap<>(DbTypes.class);
        localDbDriverFactoryGeneratorMap.put(DbTypes.POSTGRES, () -> new PostgresDbDriverFactory());
        return Collections.unmodifiableMap(localDbDriverFactoryGeneratorMap);
    }

    @NonNull
    public DbDriverFactory getDbDriverFactory(@NonNull final DbTypes dbType) throws Exception {
        if (!dbDriverFactoryGeneratorMap.containsKey(dbType)) {
            throw new Exception(String.format("dbType '%s' was not found in dbDriverFactoryGeneratorMap", dbType.name()));
        }
        final DbDriverFactoryGenerator dbDriverFactoryGenerator = dbDriverFactoryGeneratorMap.get(dbType);
        if (dbDriverFactoryGenerator == null) {
            throw new Exception(String.format("dbDriverFactoryGenerator of dbType '%s' is null", dbType.name()));
        }
        return dbDriverFactoryGenerator.generate();
    }
}