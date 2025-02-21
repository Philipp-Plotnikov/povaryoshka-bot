package telegram.commands;

import static org.mockito.Mockito.mock;
import static utilities.CommonsUtilities.getDbType;

import java.sql.Connection;
import java.sql.DriverManager;

import static mocks.DbDriverMocker.getDbDriverMock;
import static utilities.CoreUtilities.getPovaryoshkaBot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;

import models.db.DbTypes;
import telegram.bot.PovaryoshkaBot;

public class CreateDishCommandTest {
    private final DbTypes dbType = getDbType();

    private PovaryoshkaBot bot;
    private MockedStatic<DriverManager> mockedDriverManager;
    
    @Mock
    private Connection mockedDbConnection;

    // TODO: define the db type too
    // execute specified code
    @Before
    public void setUp() {
        final SilentSender silentSender = mock(SilentSender.class);
        try {
            mockedDriverManager = getDbDriverMock(dbType, mockedDbConnection);
            bot = getPovaryoshkaBot();
            bot.onRegister();
            bot.setSilentSender(silentSender);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createDishActionTest() {

    }

    @After
    public void tearDown() {
        mockedDriverManager.close();
    }
}
