package telegram.commands.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import telegram.bot.PovaryoshkaBot;
import telegram.commands.CreateDishCommand;
import telegram.commands.DeleteDishCommand;
import telegram.commands.EndCommand;
import telegram.commands.FeedbackCommand;
import telegram.commands.GetDishCommand;
import telegram.commands.UpdateDishCommand;

public class SimpleCommandFactory implements CommandFactory {
    @Override
    @NonNull
    public List<@NonNull AbilityExtension> getCommandList(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        final ArrayList<AbilityExtension> simpleCommandList = new ArrayList<>();
        simpleCommandList.add(new CreateDishCommand(povaryoshkaBot));
        simpleCommandList.add(new DeleteDishCommand(povaryoshkaBot));
        simpleCommandList.add(new UpdateDishCommand(povaryoshkaBot));
        simpleCommandList.add(new GetDishCommand(povaryoshkaBot));
        simpleCommandList.add(new FeedbackCommand(povaryoshkaBot));
        simpleCommandList.add(new EndCommand(povaryoshkaBot));
        return Collections.unmodifiableList(simpleCommandList);
    }
}