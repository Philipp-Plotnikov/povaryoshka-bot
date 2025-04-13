package telegram.commands.simple.updatedish;

import static org.mockito.Mockito.mock;
import static utilities.CommonsUtilities.getDbType;
import static utilities.CoreUtilities.loadEnvFileToSystemProperties;
import static mocks.DbDriverMocker.getDbDriverMock;
import static utilities.CoreUtilities.getPovaryoshkaBot;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

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
import telegram.commands.simple.updatedish.postgres.SimplePostgresUpdateDishCommandTester;


final public class SimpleUpdateDishCommandTest {
    @NonNull
    private final DbTypes dbType = getDbType();
    
    @NonNull
    private final Map<@NonNull DbTypes, @Nullable ISimpleTypedUpdateDishCommandTester> simpleTypedUpdateDishCommandTesterMap;

    @Nullable
    private PovaryoshkaBot bot;

    @Nullable
    private MockedStatic<DriverManager> mockedDriverManager;
    
    @Nullable
    private Connection mockedDbConnection;

    @BeforeClass
    public static void init() {
        loadEnvFileToSystemProperties();
    }

    public SimpleUpdateDishCommandTest() {
        simpleTypedUpdateDishCommandTesterMap = getSimpleTypedUpdateDishCommandTesterMap();
    }

    @NonNull
    private Map<@NonNull DbTypes, @Nullable ISimpleTypedUpdateDishCommandTester> getSimpleTypedUpdateDishCommandTesterMap() {
        final EnumMap<@NonNull DbTypes, @Nullable ISimpleTypedUpdateDishCommandTester> localSimpleTypedCommandTesterMap = new EnumMap<>(DbTypes.class);
        localSimpleTypedCommandTesterMap.put(DbTypes.POSTGRES, new SimplePostgresUpdateDishCommandTester());
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
    public void updateDishTest() throws Exception {
        if (bot == null) {
            throw new Exception("In UpdateDishCommandTest: bot is null.");
        }
        if (mockedDbConnection == null) {
            throw new Exception("In UpdateDishCommandTest: mockedDbConnection is null.");
        }
        final ISimpleTypedUpdateDishCommandTester simpleTypedUpdateDishCommandTester = getSimpleTypedUpdateDishCommandTester();
        simpleTypedUpdateDishCommandTester.updateDishTest(bot, mockedDbConnection);
    }

    @Test
    public void handleDishNameStateTest() throws Exception {
        if (bot == null) {
            throw new Exception("In UpdateDishCommandTest: bot is null.");
        }
        if (mockedDbConnection == null) {
            throw new Exception("In UpdateDishCommandTest: mockedDbConnection is null.");
        }
        final ISimpleTypedUpdateDishCommandTester simpleTypedUpdateDishCommandTester = getSimpleTypedUpdateDishCommandTester();
        simpleTypedUpdateDishCommandTester.handleDishNameStateTest(bot, mockedDbConnection);
    }

    @Test
    public void handleDishNameUpdateConfirmStateYesTest() throws Exception {
        if (bot == null) {
            throw new Exception("In UpdateDishCommandTest: bot is null.");
        }
        if (mockedDbConnection == null) {
            throw new Exception("In UpdateDishCommandTest: mockedDbConnection is null.");
        }
        final ISimpleTypedUpdateDishCommandTester simpleTypedUpdateDishCommandTester = getSimpleTypedUpdateDishCommandTester();
        simpleTypedUpdateDishCommandTester.handleDishNameUpdateConfirmStateYesTest(bot, mockedDbConnection);
    }

    @Test
    public void handleDishNameUpdateConfirmStateNoTest() throws Exception {
        if (bot == null) {
            throw new Exception("In UpdateDishCommandTest: bot is null.");
        }
        if (mockedDbConnection == null) {
            throw new Exception("In UpdateDishCommandTest: mockedDbConnection is null.");
        }
        final ISimpleTypedUpdateDishCommandTester simpleTypedUpdateDishCommandTester = getSimpleTypedUpdateDishCommandTester();
        simpleTypedUpdateDishCommandTester.handleDishNameUpdateConfirmStateNoTest(bot, mockedDbConnection);
    }

    @Test
    public void handleDishNameUpdateStateTest() throws Exception {
        if (bot == null) {
            throw new Exception("In UpdateDishCommandTest: bot is null.");
        }
        if (mockedDbConnection == null) {
            throw new Exception("In UpdateDishCommandTest: mockedDbConnection is null.");
        }
        final ISimpleTypedUpdateDishCommandTester simpleTypedUpdateDishCommandTester = getSimpleTypedUpdateDishCommandTester();
        simpleTypedUpdateDishCommandTester.handleDishNameUpdateStateTest(bot, mockedDbConnection);
    }

    @Test
    public void handleIngredientsUpdateConfirmStateYesTest() throws Exception {
        if (bot == null) {
            throw new Exception("In UpdateDishCommandTest: bot is null.");
        }
        if (mockedDbConnection == null) {
            throw new Exception("In UpdateDishCommandTest: mockedDbConnection is null.");
        }
        final ISimpleTypedUpdateDishCommandTester simpleTypedUpdateDishCommandTester = getSimpleTypedUpdateDishCommandTester();
        simpleTypedUpdateDishCommandTester.handleIngredientsUpdateConfirmStateYesTest(bot, mockedDbConnection);
    }

    @Test
    public void handleIngredientsUpdateConfirmStateNoTest() throws Exception {
        if (bot == null) {
            throw new Exception("In UpdateDishCommandTest: bot is null.");
        }
        if (mockedDbConnection == null) {
            throw new Exception("In UpdateDishCommandTest: mockedDbConnection is null.");
        }
        final ISimpleTypedUpdateDishCommandTester simpleTypedUpdateDishCommandTester = getSimpleTypedUpdateDishCommandTester();
        simpleTypedUpdateDishCommandTester.handleIngredientsUpdateConfirmStateNoTest(bot, mockedDbConnection);
    }

    @Test
    public void handleIngredientsUpdateStateTest() throws Exception {
        if (bot == null) {
            throw new Exception("In UpdateDishCommandTest: bot is null.");
        }
        if (mockedDbConnection == null) {
            throw new Exception("In UpdateDishCommandTest: mockedDbConnection is null.");
        }
        final ISimpleTypedUpdateDishCommandTester simpleTypedUpdateDishCommandTester = getSimpleTypedUpdateDishCommandTester();
        simpleTypedUpdateDishCommandTester.handleIngredientsUpdateStateTest(bot, mockedDbConnection);
    }

    @Test
    public void handleRecipeUpdateConfirmStateYesTest() throws Exception {
        if (bot == null) {
            throw new Exception("In UpdateDishCommandTest: bot is null.");
        }
        if (mockedDbConnection == null) {
            throw new Exception("In UpdateDishCommandTest: mockedDbConnection is null.");
        }
        final ISimpleTypedUpdateDishCommandTester simpleTypedUpdateDishCommandTester = getSimpleTypedUpdateDishCommandTester();
        simpleTypedUpdateDishCommandTester.handleRecipeUpdateConfirmStateYesTest(bot, mockedDbConnection);
    }

    @Test
    public void handleRecipeUpdateConfirmStateNoTest() throws Exception {
        if (bot == null) {
            throw new Exception("In UpdateDishCommandTest: bot is null.");
        }
        if (mockedDbConnection == null) {
            throw new Exception("In UpdateDishCommandTest: mockedDbConnection is null.");
        }
        final ISimpleTypedUpdateDishCommandTester simpleTypedUpdateDishCommandTester = getSimpleTypedUpdateDishCommandTester();
        simpleTypedUpdateDishCommandTester.handleRecipeUpdateConfirmStateNoTest(bot, mockedDbConnection);
    }

    @Test
    public void handleRecipeUpdateStateTest() throws Exception {
        if (bot == null) {
            throw new Exception("In UpdateDishCommandTest: bot is null.");
        }
        if (mockedDbConnection == null) {
            throw new Exception("In UpdateDishCommandTest: mockedDbConnection is null.");
        }
        final ISimpleTypedUpdateDishCommandTester simpleTypedUpdateDishCommandTester = getSimpleTypedUpdateDishCommandTester();
        simpleTypedUpdateDishCommandTester.handleRecipeUpdateStateTest(bot, mockedDbConnection);
    }

    @NonNull
    private ISimpleTypedUpdateDishCommandTester getSimpleTypedUpdateDishCommandTester() throws Exception {
        if (!simpleTypedUpdateDishCommandTesterMap.containsKey(dbType)) {
            throw new Exception(String.format("dbType '%s' was not found in typedCommandTesterMap", dbType.name()));
        }
        final ISimpleTypedUpdateDishCommandTester simpleTypedUpdateDishCommandTester = simpleTypedUpdateDishCommandTesterMap.get(dbType);
        if (simpleTypedUpdateDishCommandTester == null) {
            throw new Exception(String.format("typedCommandTesterMap of dbType '%s' is null", dbType.name()));
        }
        return simpleTypedUpdateDishCommandTester;
    }

    @After
    public void tearDown() throws IOException, Exception {
        if (bot == null) {
            throw new Exception("in tearDown bot is null");
        }
        if (mockedDriverManager == null) {
            throw new Exception("in tearDown mockedDriverManager is null");
        }
        bot.getDb().close();
        mockedDriverManager.close();
    }
}
