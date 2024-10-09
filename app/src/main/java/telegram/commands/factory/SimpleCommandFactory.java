package telegram.commands.factory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static models.commands.CommandConfig.CREATE_DISH_COMMAND_SETTINGS;
import static models.commands.CommandConfig.DELETE_DISH_COMMAND_SETTINGS;
import static models.commands.CommandConfig.END_COMMAND_SETTINGS;
import static models.commands.CommandConfig.FEEDBACK_COMMAND_SETTINGS;
import static models.commands.CommandConfig.GET_DISH_COMMAND_SETTINGS;
import static models.commands.CommandConfig.UPDATE_DISH_COMMAND_SETTINGS;
import telegram.commands.AbstractCommand;
import telegram.commands.CreateDishCommand;
import telegram.commands.DeleteDishCommand;
import telegram.commands.EndCommand;
import telegram.commands.FeedbackCommand;
import telegram.commands.GetDishCommand;
import telegram.commands.UpdateDishCommand;

public class SimpleCommandFactory implements CommandFactory {
    @Override
    public Map<String, AbstractCommand> getCommandMap() {
        final HashMap<String, AbstractCommand> simpleCommandMap = new HashMap<>();
        simpleCommandMap.put(
            CREATE_DISH_COMMAND_SETTINGS.commandName(),
            new CreateDishCommand(
                CREATE_DISH_COMMAND_SETTINGS.commandName(),
                CREATE_DISH_COMMAND_SETTINGS.commandDescription()
            )
        );
        simpleCommandMap.put(
            DELETE_DISH_COMMAND_SETTINGS.commandName(),
            new DeleteDishCommand(
                DELETE_DISH_COMMAND_SETTINGS.commandName(),
                DELETE_DISH_COMMAND_SETTINGS.commandDescription()
            )
        );
        simpleCommandMap.put(
            UPDATE_DISH_COMMAND_SETTINGS.commandName(),
            new UpdateDishCommand(
                UPDATE_DISH_COMMAND_SETTINGS.commandName(),
                UPDATE_DISH_COMMAND_SETTINGS.commandDescription()
            )
        );
        simpleCommandMap.put(
            GET_DISH_COMMAND_SETTINGS.commandName(),
            new GetDishCommand(
                GET_DISH_COMMAND_SETTINGS.commandName(),
                GET_DISH_COMMAND_SETTINGS.commandDescription()
            )
        );
        simpleCommandMap.put(
            FEEDBACK_COMMAND_SETTINGS.commandName(),
            new FeedbackCommand(
                FEEDBACK_COMMAND_SETTINGS.commandName(),
                FEEDBACK_COMMAND_SETTINGS.commandDescription()
            )
        );
        simpleCommandMap.put(
            END_COMMAND_SETTINGS.commandName(),
            new EndCommand(
                END_COMMAND_SETTINGS.commandName(),
                END_COMMAND_SETTINGS.commandDescription()
            )
        );
        return Collections.unmodifiableMap(simpleCommandMap);
    }
}