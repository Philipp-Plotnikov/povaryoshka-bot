package telegram.commands.simple.feedback;

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
import telegram.commands.simple.feedback.postgres.SimplePostgresFeedbackCommandTester;

import static mocks.DbDriverMocker.getDbDriverMock;
import static org.mockito.Mockito.mock;
import static utilities.CommonsUtilities.getDbType;
import static utilities.CoreUtilities.getPovaryoshkaBot;
import static utilities.CoreUtilities.loadEnvFileToSystemProperties;


public class SimpleFeedbackCommandTest {
    @NonNull
    private final DbTypes dbType = getDbType();

    @NonNull
    private final Map<@NonNull DbTypes, @Nullable ISimpleTypedFeedbackCommandTester> simpleTypedFeedbackCommandTesterMap;

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

    public SimpleFeedbackCommandTest() {
        simpleTypedFeedbackCommandTesterMap = getSimpleTypedFeedbackCommandTesterMap();
    }

    @NonNull
    private Map<@NonNull DbTypes, @Nullable ISimpleTypedFeedbackCommandTester> getSimpleTypedFeedbackCommandTesterMap() {
        final EnumMap<@NonNull DbTypes, @Nullable ISimpleTypedFeedbackCommandTester> localSimpleTypedCommandTesterMap = new EnumMap<>(DbTypes.class);
        localSimpleTypedCommandTesterMap.put(DbTypes.POSTGRES, new SimplePostgresFeedbackCommandTester());
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
    public void feedbackTest() throws Exception {
        if (bot == null) {
            throw new Exception("In FeedbackDishCommandTest: bot is null.");
        }
        if (mockedDbConnection == null) {
            throw new Exception("In FeedbackDishCommandTest: mockedDbConnection is null.");
        }
        final ISimpleTypedFeedbackCommandTester simpleTypedFeedbackCommandTester = getSimpleTypedFeedbackCommandTester();
        simpleTypedFeedbackCommandTester.feedbackTest(bot, mockedDbConnection);
    }

    @NonNull
    private ISimpleTypedFeedbackCommandTester getSimpleTypedFeedbackCommandTester() throws Exception {
        if (!simpleTypedFeedbackCommandTesterMap.containsKey(dbType)) {
            throw new Exception(String.format("dbType '%s' was not found in typedCommandTesterMap", dbType.name()));
        }
        final ISimpleTypedFeedbackCommandTester simpleTypedFeedbackCommandTester = simpleTypedFeedbackCommandTesterMap.get(dbType);
        if (simpleTypedFeedbackCommandTester == null) {
            throw new Exception(String.format("typedCommandTesterMap of dbType '%s' is null", dbType.name()));
        }
        return simpleTypedFeedbackCommandTester;
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
