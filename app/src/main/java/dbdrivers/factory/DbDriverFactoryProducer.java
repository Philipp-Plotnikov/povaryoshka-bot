package dbdrivers.factory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import models.db.DbTypes;
import models.db.drivers.DbDriverFactoryGenerator;


public class DbDriverFactoryProducer {
    private final Map<DbTypes, DbDriverFactoryGenerator> dbDriverFactoryGeneratorMap;

    public DbDriverFactoryProducer() {
        dbDriverFactoryGeneratorMap = getDbDriverFactoryGeneratorMap();
    }

    private Map<DbTypes, DbDriverFactoryGenerator> getDbDriverFactoryGeneratorMap() {
        final EnumMap<DbTypes, DbDriverFactoryGenerator> localDbDriverFactoryGeneratorMap = new EnumMap<>(DbTypes.class);
        localDbDriverFactoryGeneratorMap.put(DbTypes.POSTGRES, () -> new PostgresDbDriverFactory());
        return Collections.unmodifiableMap(localDbDriverFactoryGeneratorMap);
    }

    public DbDriverFactory getDbDriverFactory(final DbTypes dbType) throws Exception {
        if (!dbDriverFactoryGeneratorMap.containsKey(dbType)) {
            throw new Exception(String.format("%s was not found in dbDriverFactoryGeneratorMap", dbType.name()));
        }
        return dbDriverFactoryGeneratorMap.get(dbType).generate();
    }
}