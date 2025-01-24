package com.example.Telegram.service.console;

import java.util.HashMap;
import java.util.Map;

public class CommandInvoker {
    private Map<CommandType, Command> commands = new HashMap<>();

    public void register(CommandType commandType, Command command) {
        commands.put(commandType, command);
    }

    public void execute(CommandType commandType) {
        Command command = commands.get(commandType);
        if (command != null) {
            command.execute();
        }
    }
}
