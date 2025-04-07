package telegram.commands.simple.createdish;

import java.sql.Connection;
import java.sql.SQLException;

import org.checkerframework.checker.nullness.qual.NonNull;

import models.exceptions.db.sqlops.NotFoundUserContextException;
import telegram.bot.PovaryoshkaBot;


public interface ISimpleTypedCreateDishCommandTester {
    void createDishTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception;
    
    void handleDishNameUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, NotFoundUserContextException, Exception;
    
    void handleIngredientsUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, NotFoundUserContextException, Exception;
    
    void handleRecipeUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, NotFoundUserContextException, Exception;
    
    void isInCreateDishContextTruthyTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception;

    void isInCreateDishContextFalsyTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception;
}
