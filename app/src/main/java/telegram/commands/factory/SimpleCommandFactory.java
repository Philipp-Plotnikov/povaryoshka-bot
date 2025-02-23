package telegram.commands.factory;

import static models.commands.CommandConfig.CREATE_DISH_COMMAND_SETTINGS;
import static models.commands.CommandConfig.DELETE_DISH_COMMAND_SETTINGS;
import static models.commands.CommandConfig.END_COMMAND_SETTINGS;
import static models.commands.CommandConfig.FEEDBACK_COMMAND_SETTINGS;
import static models.commands.CommandConfig.GET_DISH_COMMAND_SETTINGS;
import static models.commands.CommandConfig.START_COMMAND_SETTINGS;
import static models.commands.CommandConfig.UPDATE_DISH_COMMAND_SETTINGS;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import telegram.bot.PovaryoshkaBot;
import telegram.commands.CreateDishCommand;
import telegram.commands.DeleteDishCommand;
import telegram.commands.EndCommand;
import telegram.commands.FeedbackCommand;
import telegram.commands.GetDishCommand;
import telegram.commands.UpdateDishCommand;
import telegram.commands.StartCommand;


public class SimpleCommandFactory implements ICommandFactory {
    @NonNull
    public Map<String, @Nullable AbilityExtension> getCommandMap(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        final HashMap<String, AbilityExtension> simpleCommandMap = new HashMap<>();
        simpleCommandMap.put(
            START_COMMAND_SETTINGS.commandName(),
            new StartCommand(povaryoshkaBot)
        );
        simpleCommandMap.put(
            CREATE_DISH_COMMAND_SETTINGS.commandName(),
            new CreateDishCommand(povaryoshkaBot)
        );
        simpleCommandMap.put(
            DELETE_DISH_COMMAND_SETTINGS.commandName(),
            new DeleteDishCommand(povaryoshkaBot)
        );
        simpleCommandMap.put(
            UPDATE_DISH_COMMAND_SETTINGS.commandName(),
            new UpdateDishCommand(povaryoshkaBot)
        );
        simpleCommandMap.put(
            GET_DISH_COMMAND_SETTINGS.commandName(),
            new GetDishCommand(povaryoshkaBot)
        );
        simpleCommandMap.put(
            FEEDBACK_COMMAND_SETTINGS.commandName(),
            new FeedbackCommand(povaryoshkaBot)
        );
        simpleCommandMap.put(
            END_COMMAND_SETTINGS.commandName(),
            new EndCommand(povaryoshkaBot)
        );
        return Collections.unmodifiableMap(simpleCommandMap);
    }
}