package telegram.commands.simple.updatedish;

import java.sql.Connection;
import java.sql.SQLException;

import org.checkerframework.checker.nullness.qual.NonNull;

import models.exceptions.db.sqlops.NotFoundUserContextException;
import telegram.bot.PovaryoshkaBot;


public interface ISimpleTypedUpdateDishCommandTester {
    void updateDishTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws SQLException, Exception;
    
    void handleDishNameStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception;
    
    void handleDishNameUpdateConfirmStateYesTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception;

    void handleDishNameUpdateConfirmStateNoTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception;

    void handleDishNameUpdateConfirmStateInvalidTextTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception;
    
    void handleDishNameUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception;
    
    void handleIngredientsUpdateConfirmStateYesTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception;

    void handleIngredientsUpdateConfirmStateNoTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception;

    void handleIngredientsUpdateConfirmStateInvalidTextTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception;

    void handleIngredientsUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws NotFoundUserContextException, SQLException, Exception;

    void handleRecipeUpdateConfirmStateYesTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    );

    void handleRecipeUpdateConfirmStateNoTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    );

    void handleRecipeUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    );
}
