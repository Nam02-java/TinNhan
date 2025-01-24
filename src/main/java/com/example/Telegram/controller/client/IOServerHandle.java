package com.example.Telegram.controller.client;


import com.example.Telegram.exception.ConnectionLostException;
import com.example.Telegram.service.console.Command;
import com.example.Telegram.service.console.CommandInvoker;
import com.example.Telegram.service.console.CommandType;
import com.example.Telegram.service.console.NetworkState;
import com.example.Telegram.service.http.restful.MessageHistoryApiCaller;
import com.example.Telegram.service.http.restful.MessageStatusApiCaller;
import com.example.Telegram.service.http.restful.MessageUnreadApiCaller;
import com.example.Telegram.service.json.JsonMessageBuilder;
import com.example.Telegram.service.json.JsonMessageParser;
import com.example.Telegram.service.messages.MessageQueueManager;
import com.example.Telegram.service.socket.ClientInitializer;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import static java.lang.System.out;

public class IOServerHandle {

    private ClientInitializer clientInitializer;
    private BufferedReader inputFromServer;
    private DataOutputStream outputToServer;
    private BufferedReader userInput;
    private String currentUsername;
    private Socket socket;
    private CommandInvoker commandInvoker;
    private JsonMessageBuilder jsonMessageBuilder;
    private JsonMessageParser jsonMessageParser;
    private MessageStatusApiCaller messageStatusApiCaller;
    private MessageHistoryApiCaller messageHistoryApiCaller;
    private MessageUnreadApiCaller messageUnreadApiCaller;
    private MessageQueueManager messageQueueManager;
    private NetworkState networkState;

    public IOServerHandle(ClientInitializer clientInitializer, Socket socket, BufferedReader inputFromServer, DataOutputStream outputToServer, BufferedReader userInput,
                          String userName,
                          CommandInvoker commandInvoker,
                          JsonMessageBuilder jsonMessageBuilder, JsonMessageParser jsonMessageParser,
                          MessageStatusApiCaller messageStatusApiCaller, MessageHistoryApiCaller messageHistoryApiCaller, MessageUnreadApiCaller messageUnreadApiCaller,
                          MessageQueueManager messageQueueManager,
                          NetworkState networkState) {
        this.clientInitializer = clientInitializer;
        this.socket = socket;
        this.inputFromServer = inputFromServer;
        this.outputToServer = outputToServer;
        this.userInput = userInput;
        this.currentUsername = userName;
        this.commandInvoker = commandInvoker;
        this.jsonMessageBuilder = jsonMessageBuilder;
        this.jsonMessageParser = jsonMessageParser;
        this.messageStatusApiCaller = messageStatusApiCaller;
        this.messageHistoryApiCaller = messageHistoryApiCaller;
        this.messageUnreadApiCaller = messageUnreadApiCaller;
        this.messageQueueManager = messageQueueManager;
        this.networkState = networkState;
    }

    public void initialize() throws IOException {

        messageUnreadApiCaller.fetchUnreadMessagesFromServer(currentUsername);
        new Thread(() -> {
            while (true) {
                try {
                    if (!networkState.isSimulateNetworkLoss()) {
                        handleServerMessages(inputFromServer);
                    }

                } catch (ConnectionLostException e) {
                    if (!networkState.isSimulateNetworkLoss()) {
                        out.println(e.getMessage());
                        Socket newSocket = clientInitializer.reconnect();
                        updateIO(newSocket);
                    }
                }
            }
        }).start();


        String messageToServer;
        try {
            while ((messageToServer = userInput.readLine()) != null) {
                CommandType commandType = CommandType.fromString(messageToServer);

                if (commandType != null) {
                    commandInvoker.execute(commandType);
                } else {
                    if (networkState.isSimulateNetworkLoss()) {
                        JSONObject jsonObjectMessageHasNotBeenSent = jsonMessageBuilder.build(currentUsername, messageToServer);
                        messageQueueManager.addMessage(new JSONObject(jsonObjectMessageHasNotBeenSent.toString()));
                        String messagesHasNotBeenSent = senderMessages(jsonObjectMessageHasNotBeenSent);
                        System.out.println("Messages has not been sent - " + messagesHasNotBeenSent);
                    } else {
                        JSONObject messageJson = jsonMessageBuilder.build(currentUsername, messageToServer);
                        outputToServer.writeBytes(messageJson.toString() + "\n");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleServerMessages(BufferedReader inputFromServer) throws ConnectionLostException {
        String messageFromServer = null;
        try {

            messageFromServer = inputFromServer.readLine();

            if (!networkState.isSimulateNetworkLoss()) {
                jsonMessageParser.parseJson(messageFromServer);
                Long messageId = jsonMessageParser.getId(); // id messages
                String senderName = jsonMessageParser.getUsername(); // sender name
                String messageContent = jsonMessageParser.getMessage(); // messages
                LocalDateTime sentAt = jsonMessageParser.getSentAt(); // sent at
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedSentAt = sentAt.format(formatter);

                out.println(String.format("[%s] %s: %s", formattedSentAt, senderName, messageContent)); // seen

                if (!currentUsername.equals(senderName)) {
                    messageStatusApiCaller.setMessageStatusToSeen(messageId);
                }
            }
        } catch (IOException e) {
            throw new ConnectionLostException("Connection to server lost", e);
        }
    }

    private String senderMessages(JSONObject jsonObject) {
        String string;
        String sentAt = jsonObject.getString("sentAt");
        String formattedSentAt = sentAt.substring(0, 10) + " " + sentAt.substring(11, 19); // "YYYY-MM-DD HH:MM:SS"

        string = String.format
                ("[%s] %s: %s", formattedSentAt,
                        jsonObject.getString("username"),
                        jsonObject.getString("message"));

        return string;

    }


    private void updateIO(Socket newSocket) {
        try {
            inputFromServer = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));
            outputToServer = new DataOutputStream(newSocket.getOutputStream());
        } catch (IOException e) {
            out.println("Failed to update IO streams: " + e.getMessage());
            throw new RuntimeException("Failed to update IO streams, cannot proceed", e);
        }
    }
}


