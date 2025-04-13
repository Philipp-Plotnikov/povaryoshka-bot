package utilities;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import telegram.commands.CreateDishCommand;
import telegram.commands.UpdateDishCommand;


final public class CommandUtilities {
    public static boolean isCreateDishCommand(@NonNull AbilityExtension untypedCommand) {
        return untypedCommand != null && (untypedCommand instanceof CreateDishCommand);
    }

    public static boolean isUpdateDishCommand(@NonNull AbilityExtension untypedCommand) {
        return untypedCommand != null && (untypedCommand instanceof UpdateDishCommand);
    }
}
