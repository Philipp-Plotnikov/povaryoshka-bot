package telegram.commands.simple.getdish;

import models.db.DbTypes;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;
import telegram.bot.PovaryoshkaBot;
import telegram.commands.simple.getdish.postgres.SimplePostgresGetDishCommandTester;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import static mocks.DbDriverMocker.getDbDriverMock;
import static org.mockito.Mockito.mock;
import static utilities.CommonsUtilities.getDbType;
import static utilities.CoreUtilities.getPovaryoshkaBot;
import static utilities.CoreUtilities.loadEnvFileToSystemProperties;

public class SimpleGetDishCommandTest{
    @NonNull
    private final DbTypes dbType = getDbType();

    @NonNull
    private final Map<@NonNull DbTypes, @Nullable ISimpleTypedGetDishCommandTester> simpleTypedGetDishCommandTesterMap;

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

    public SimpleGetDishCommandTest() {
        simpleTypedGetDishCommandTesterMap = getSimpleTypedGetDishCommandTesterMap();
    }

    @NonNull
    private Map<@NonNull DbTypes, @Nullable ISimpleTypedGetDishCommandTester> getSimpleTypedGetDishCommandTesterMap() {
        final EnumMap<@NonNull DbTypes, @Nullable ISimpleTypedGetDishCommandTester> localSimpleTypedCommandTesterMap = new EnumMap<>(DbTypes.class);
        localSimpleTypedCommandTesterMap.put(DbTypes.POSTGRES, new SimplePostgresGetDishCommandTester());
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
    public void getDishTest() throws Exception {
        if (bot == null) {
            throw new Exception("In UpdateDishCommandTest: bot is null.");
        }
        if (mockedDbConnection == null) {
            throw new Exception("In UpdateDishCommandTest: mockedDbConnection is null.");
        }
        final ISimpleTypedGetDishCommandTester simpleTypedGetDishCommandTester = getSimpleTypedGetDishCommandTester();
        simpleTypedGetDishCommandTester.getDishTest(bot, mockedDbConnection);
    }

    @Test
    public void getFormatDishInfoTest() throws Exception {
        if (bot == null) {
            throw new Exception("In UpdateDishCommandTest: bot is null.");
        }
        if (mockedDbConnection == null) {
            throw new Exception("In UpdateDishCommandTest: mockedDbConnection is null.");
        }
        final ISimpleTypedGetDishCommandTester simpleTypedGetDishCommandTester = getSimpleTypedGetDishCommandTester();
        simpleTypedGetDishCommandTester.getFormatDishInfoTest(bot, mockedDbConnection);
    }

    @NonNull
    private ISimpleTypedGetDishCommandTester getSimpleTypedGetDishCommandTester() throws Exception {
        if (!simpleTypedGetDishCommandTesterMap.containsKey(dbType)) {
            throw new Exception(String.format("dbType '%s' was not found in typedCommandTesterMap", dbType.name()));
        }
        final ISimpleTypedGetDishCommandTester simpleTypedGetDishCommandTester = simpleTypedGetDishCommandTesterMap.get(dbType);
        if (simpleTypedGetDishCommandTester == null) {
            throw new Exception(String.format("typedCommandTesterMap of dbType '%s' is null", dbType.name()));
        }
        return simpleTypedGetDishCommandTester;
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
