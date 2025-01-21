package com.example.Telegram.controller.client;


import com.example.Telegram.exception.ConnectionLostException;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import static java.lang.System.out;

public class IOServerHandle {

    private ClientInitializer clientInitializer;
    private BufferedReader inputFromServer;
    private DataOutputStream outputToServer;
    private BufferedReader userInput;
    private String currentUsername;
    private Socket socket;
    private boolean simulateNetworkLoss = false;
    private JsonMessageBuilder jsonMessageBuilder;
    private JsonMessageParser jsonMessageParser;
    private MessageStatusApiCaller messageStatusApiCaller;
    private MessageHistoryApiCaller messageHistoryApiCaller;
    private MessageUnreadApiCaller messageUnreadApiCaller;
    private MessageQueueManager messageQueueManager;


    public IOServerHandle(ClientInitializer clientInitializer, Socket socket, String userName,
                          JsonMessageBuilder jsonMessageBuilder, JsonMessageParser jsonMessageParser,
                          MessageStatusApiCaller messageStatusApiCaller, MessageHistoryApiCaller messageHistoryApiCaller, MessageUnreadApiCaller messageUnreadApiCaller,
                          MessageQueueManager messageQueueManager) {
        this.clientInitializer = clientInitializer;
        this.socket = socket;
        this.currentUsername = userName;
        this.userInput = new BufferedReader(new InputStreamReader(System.in));
        this.jsonMessageBuilder = jsonMessageBuilder;
        this.jsonMessageParser = jsonMessageParser;
        this.messageStatusApiCaller = messageStatusApiCaller;
        this.messageHistoryApiCaller = messageHistoryApiCaller;
        this.messageUnreadApiCaller = messageUnreadApiCaller;
        this.messageQueueManager = messageQueueManager;
    }

    public void initialize() throws IOException {
        inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outputToServer = new DataOutputStream(socket.getOutputStream());

        messageUnreadApiCaller.fetchUnreadMessagesFromServer(currentUsername);
        new Thread(() -> {
            while (true) {
                try {
                    if (!simulateNetworkLoss) {
                        handleServerMessages(inputFromServer);
                    }

                } catch (ConnectionLostException e) {
                    if (!simulateNetworkLoss) {
                        out.println(e.getMessage());
                        Socket newSocket = clientInitializer.reconnect();
                        updateIO(newSocket);
                    }
                }
            }
        }).start();


        String messageToServer;
        int currentPage = 0;
        try {
            while ((messageToServer = userInput.readLine()) != null) {

                /**
                 * When the user enters "1" into the console -> the program will see it as a signal that the user requests to load history
                 */
                if ("1".equals(messageToServer)) {
                    List<String> history = messageHistoryApiCaller.fetchChatHistoryFromServer(currentPage);
                    if (history.isEmpty()) {
                        out.println("No more history available");
                    } else {
                        out.println("-----------------history messages-----------------");
                        for (String message : history) {
                            out.println(message);
                        }
                        out.println("-----------------history messages-----------------");
                        currentPage++; // Increase the current page
                    }

                    /**
                     * When the user enters "2" words into the console -> the program will simulate the case where the user's client loses internet connection
                     */
                } else if ("2".equals(messageToServer)) {
                    simulateNetworkLoss = true;
                    out.println("Internet connection loss simulation starts working");

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (simulateNetworkLoss) {
                                out.println("Lost internet beyond waiting time , exiting...");
                                System.exit(0);
                            }
                        }
                    }, 20000);

                    /**
                     * When the user enters "3" words into the console -> the program will simulate the case of the user's client successfully connecting to the internet
                     */
                } else if ("3".equals(messageToServer)) {
                    if (simulateNetworkLoss) {
                        simulateNetworkLoss = false;
                        out.println("internet connection loss simulation ends");


                        messageUnreadApiCaller.fetchUnreadMessagesFromServer(currentUsername);

                        // Push all pending messages in the queue to server
                        while (messageQueueManager.hasPendingMessages()) {
                            JSONObject jsonMessageQueueManager = messageQueueManager.getNextMessage();

                            outputToServer.writeBytes(jsonMessageQueueManager.toString() + "\n");
                        }
                    }


                    /**
                     * real time chat
                     */
                } else {
                    if (simulateNetworkLoss) {
                        JSONObject jsonObjectMessageHasNotBeenSent = jsonMessageBuilder.build(currentUsername, messageToServer);
                        messageQueueManager.addMessage(new JSONObject(jsonObjectMessageHasNotBeenSent.toString()));
                        String messagesHasNotBeenSent = senderMessages(jsonObjectMessageHasNotBeenSent);
                        out.println("Messages has not been sent - " + messagesHasNotBeenSent);
                        continue;
                    }
                    JSONObject messageJson = jsonMessageBuilder.build(currentUsername, messageToServer);
                    outputToServer.writeBytes(messageJson.toString() + "\n");

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

            if (!simulateNetworkLoss) {
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


