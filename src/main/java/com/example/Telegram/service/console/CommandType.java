package com.example.Telegram.service.console;

public enum CommandType {
    LOAD_HISTORY("history"),
    SIMULATE_NETWORK_LOSS("simulate_network_loss"),
    RESTORE_NETWORK("restore_network");

    private final String commandKeyword;

    CommandType(String commandKeyword) {
        this.commandKeyword = commandKeyword;
    }

    public String getCommandKeyword() {
        return commandKeyword;
    }

    public static CommandType fromString(String input) {
        for (CommandType type : CommandType.values()) {
            if (type.commandKeyword.equalsIgnoreCase(input)) {
                return type;
            }
        }
        return null;
    }
}
