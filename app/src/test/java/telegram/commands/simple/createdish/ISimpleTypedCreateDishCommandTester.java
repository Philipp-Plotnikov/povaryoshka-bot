package telegram.commands.simple.createdish;

import java.sql.Connection;

import org.checkerframework.checker.nullness.qual.NonNull;

import telegram.bot.PovaryoshkaBot;


public interface ISimpleTypedCreateDishCommandTester {
    void createDishTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    ) throws Exception;
    
    void handleDishNameUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    );
    
    void handleIngredientsUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    );
    
    void handleRecipeUpdateStateTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    );
    
    void isInCreateDishContextTest(
        @NonNull final PovaryoshkaBot bot,
        @NonNull final Connection mockedDbConnection
    );
}
