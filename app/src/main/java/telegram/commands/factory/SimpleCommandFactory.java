package telegram.commands.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import telegram.bot.PovaryoshkaBot;
import telegram.commands.*;

public class SimpleCommandFactory implements ICommandFactory {
    @Override
    @NonNull
    public List<@NonNull AbilityExtension> getCommandList(@NonNull final PovaryoshkaBot povaryoshkaBot) {
        final ArrayList<AbilityExtension> simpleCommandList = new ArrayList<>();
        simpleCommandList.add(new StartCommand(povaryoshkaBot));
        simpleCommandList.add(new CreateDishCommand(povaryoshkaBot));
        simpleCommandList.add(new DeleteDishCommand(povaryoshkaBot));
        simpleCommandList.add(new UpdateDishCommand(povaryoshkaBot));
        simpleCommandList.add(new GetDishCommand(povaryoshkaBot));
        simpleCommandList.add(new FeedbackCommand(povaryoshkaBot));
        simpleCommandList.add(new EndCommand(povaryoshkaBot));
        simpleCommandList.add(new DefaultReplyCommand(povaryoshkaBot));
        return Collections.unmodifiableList(simpleCommandList);
    }
}