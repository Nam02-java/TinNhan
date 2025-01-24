package com.example.Telegram.service.console;

import com.example.Telegram.service.http.restful.MessageHistoryApiCaller;

import java.util.List;

public class LoadHistoryCommand implements Command {
    private final MessageHistoryApiCaller messageHistoryApiCaller;
    private int currentPage;

    public LoadHistoryCommand(MessageHistoryApiCaller messageHistoryApiCaller) {
        this.messageHistoryApiCaller = messageHistoryApiCaller;
        this.currentPage = 0;
    }

    @Override
    public void execute() {
        List<String> history = messageHistoryApiCaller.fetchChatHistoryFromServer(currentPage);
        if (history.isEmpty()) {
            System.out.println("No more history available");
        } else {
            System.out.println("-----------------history messages-----------------");
            for (String message : history) {
                System.out.println(message);
            }
            System.out.println("-----------------history messages-----------------");
            currentPage++;
        }
    }
}
