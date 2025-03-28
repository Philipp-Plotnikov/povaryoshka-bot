package models.commands;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CommandMap {

    private static CommandMap instance;
    private final List<String> commandList;

    private CommandMap() {
        commandList = new ArrayList<>();
        commandList.add("/create");
        commandList.add("/delete");
        commandList.add("/end");
        commandList.add("/feedback");
        commandList.add("/get");
        commandList.add("/update");
        commandList.add("/start");
    }

    public static CommandMap getInstance() {
        if (instance == null) {
            instance = new CommandMap();
        }
        return instance;
    }

    @NonNull
    public List<String> getCommandList() {
        return commandList;
    }

    public boolean containsCommand(String command) {
        return commandList.contains(command);
    }
}
