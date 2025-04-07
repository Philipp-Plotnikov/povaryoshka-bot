package utilities;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import telegram.commands.CreateDishCommand;


final public class CommandUtilities {
    public static boolean isCreateDishCommand(@NonNull AbilityExtension untypedCommand) {
        return untypedCommand != null && (untypedCommand instanceof CreateDishCommand);
    }
}
