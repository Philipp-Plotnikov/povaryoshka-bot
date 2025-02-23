package telegram.commands.simple.createdish;

import static org.mockito.Mockito.mock;
import static utilities.CommonsUtilities.getDbType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import static mocks.DbDriverMocker.getDbDriverMock;
import static utilities.CoreUtilities.getPovaryoshkaBot;
import static utilities.CoreUtilities.loadEnvFileToSystemProperties;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;

import models.db.DbTypes;
import telegram.bot.PovaryoshkaBot;
import telegram.commands.simple.createdish.postgres.SimplePostgresDishCommandTester;


// TODO: why @Mock doesnt work
public class SimpleCreateDishCommandTest {
    @NonNull
    private final DbTypes dbType = getDbType();
    
    @NonNull
    private final Map<@NonNull DbTypes, @Nullable SimpleTypedCreateDishCommandTester> simpleTypedCreateDishCommandTesterMap;

    @Nullable
    private PovaryoshkaBot bot;

    @Nullable
    private MockedStatic<DriverManager> mockedDriverManager;
    
    @NonNull
    private Connection mockedDbConnection;

    @BeforeClass
    public static void init() {
        loadEnvFileToSystemProperties();
    }

    public SimpleCreateDishCommandTest() {
        simpleTypedCreateDishCommandTesterMap = getSimpleTypedCreateDishCommandTesterMap();
    }

    @NonNull
    private Map<@NonNull DbTypes, @Nullable SimpleTypedCreateDishCommandTester> getSimpleTypedCreateDishCommandTesterMap() {
        final EnumMap<@NonNull DbTypes, @Nullable SimpleTypedCreateDishCommandTester> localSimpleTypedCommandTesterMap = new EnumMap<>(DbTypes.class);
        localSimpleTypedCommandTesterMap.put(DbTypes.POSTGRES, new SimplePostgresDishCommandTester());
        return Collections.unmodifiableMap(localSimpleTypedCommandTesterMap);
    }

    @Before
    public void setup() {
        final SilentSender silentSender = mock(SilentSender.class);
        try {
            mockedDbConnection = mock(Connection.class);
            mockedDriverManager = getDbDriverMock(dbType, mockedDbConnection);
            bot = getPovaryoshkaBot();
            bot.setSilentSender(silentSender);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createDishTest() throws Exception {
        if (bot == null) {
            throw new Exception("In CreateDishCommandTest bot is null");
        }
        final SimpleTypedCreateDishCommandTester simpleTypedCreateDishCommandTester = getSimpleTypedCreateDishCommandTester();
        simpleTypedCreateDishCommandTester.createDishTest(bot, mockedDbConnection);
    }

    @Test
    public void handleDishNameUpdateStateTest() throws Exception {
        if (bot == null) {
            throw new Exception("In CreateDishCommandTest bot is null");
        }
        final SimpleTypedCreateDishCommandTester simpleTypedCreateDishCommandTester = getSimpleTypedCreateDishCommandTester();
        simpleTypedCreateDishCommandTester.handleDishNameUpdateStateTest(bot, mockedDbConnection);
    }

    @Test
    public void handleIngredientsUpdateStateTest() throws Exception {
        if (bot == null) {
            throw new Exception("In CreateDishCommandTest bot is null");
        }
        final SimpleTypedCreateDishCommandTester simpleTypedCreateDishCommandTester = getSimpleTypedCreateDishCommandTester();
        simpleTypedCreateDishCommandTester.handleIngredientsUpdateStateTest(bot, mockedDbConnection);
    }

    @Test
    public void handleRecipeUpdateStateTest() throws Exception {
        if (bot == null) {
            throw new Exception("In CreateDishCommandTest bot is null");
        }
        final SimpleTypedCreateDishCommandTester simpleTypedCreateDishCommandTester = getSimpleTypedCreateDishCommandTester();
        simpleTypedCreateDishCommandTester.handleRecipeUpdateStateTest(bot, mockedDbConnection);
    }

    @Test
    public void isInCreateDishContextTest() throws Exception {
        if (bot == null) {
            throw new Exception("In CreateDishCommandTest bot is null");
        }
        final SimpleTypedCreateDishCommandTester simpleTypedCreateDishCommandTester = getSimpleTypedCreateDishCommandTester();
        simpleTypedCreateDishCommandTester.isInCreateDishContextTest(bot, mockedDbConnection);
    }

    @NonNull
    private SimpleTypedCreateDishCommandTester getSimpleTypedCreateDishCommandTester() throws Exception {
        if (!simpleTypedCreateDishCommandTesterMap.containsKey(dbType)) {
            throw new Exception(String.format("dbType '%s' was not found in typedCommandTesterMap", dbType.name()));
        }
        final SimpleTypedCreateDishCommandTester simpleTypedCreateDishCommandTester = simpleTypedCreateDishCommandTesterMap.get(dbType);
        if (simpleTypedCreateDishCommandTester == null) {
            throw new Exception(String.format("typedCommandTesterMap of dbType '%s' is null", dbType.name()));
        }
        return simpleTypedCreateDishCommandTester;
    }

    @After
    public void tearDown() throws IOException {
        if (mockedDriverManager == null) {
            return;
        }
        // TODO: Refactor
        mockedDriverManager.close();
        bot.getDb().close();
    }
}
