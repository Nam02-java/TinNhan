package com.example.Telegram.service.console;

import com.example.Telegram.service.http.restful.MessageUnreadApiCaller;
import com.example.Telegram.service.messages.MessageQueueManager;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;

public class RestoreNetworkCommand implements Command {
    private NetworkState networkState;
    private MessageQueueManager messageQueueManager;
    private MessageUnreadApiCaller messageUnreadApiCaller;
    private DataOutputStream outputToServer;

    private String currentUsername;

    public RestoreNetworkCommand(NetworkState networkState, MessageQueueManager messageQueueManager, MessageUnreadApiCaller messageUnreadApiCaller, DataOutputStream outputToServer, String currentUsername) {
        this.networkState = networkState;
        this.messageQueueManager = messageQueueManager;
        this.messageUnreadApiCaller = messageUnreadApiCaller;
        this.outputToServer = outputToServer;
        this.currentUsername = currentUsername;
    }

    @Override
    public void execute() {
        if (networkState.isSimulateNetworkLoss()) {

            networkState.setSimulateNetworkLoss(false);

            System.out.println("Internet connection loss simulation ends");

            messageUnreadApiCaller.fetchUnreadMessagesFromServer(currentUsername);

            while (messageQueueManager.hasPendingMessages()) {
                JSONObject jsonMessageQueueManager = messageQueueManager.getNextMessage();
                try {
                    outputToServer.writeBytes(jsonMessageQueueManager.toString() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}