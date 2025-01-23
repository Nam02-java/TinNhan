package com.example.Telegram.service.socket;

import com.example.Telegram.controller.client.IOServerHandle;
import com.example.Telegram.exception.ConnectionLostException;
import com.example.Telegram.service.http.restful.MessageHistoryApiCaller;
import com.example.Telegram.service.http.restful.MessageStatusApiCaller;
import com.example.Telegram.service.http.restful.MessageUnreadApiCaller;
import com.example.Telegram.service.http.sse.NotificationApiCaller;
import com.example.Telegram.service.http.restful.UserSessionApiCaller;
import com.example.Telegram.service.json.JsonMessageBuilder;
import com.example.Telegram.service.json.JsonMessageParser;
import com.example.Telegram.service.messages.MessageQueueManager;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientInitializer {
    private final String SERVER_ADDRESS = "localhost";
    private final int SERVER_PORT = 8081;
    private final String SERVER_URL_MESSAGES = "http://localhost:8080/messages";
    private String SERVER_URL_SESSIONS = "http://localhost:8080/api/sessions";
    private String userName;
    private String device_info;
    private String ipAddress;
    private ExecutorService executorService;
    private UserSessionApiCaller userSessionApiCaller;
    private MessageStatusApiCaller messageStatusApiCaller;
    private MessageHistoryApiCaller messageHistoryApiCaller;
    private MessageUnreadApiCaller messageUnreadApiCaller;
    private MessageQueueManager messageQueueManager;

    public ClientInitializer(String userName, String device_info, String ipAddress) {
        this.userName = userName;
        this.device_info = device_info;
        this.ipAddress = ipAddress;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void initiateConnectionToServer() throws IOException {
        // Set up client connection to server
        Socket socket = connectServer();

        /**
         * Swap port and local port to socket as server-side socket
         * to synchronize with SocketManager.class and currentSocket at IOClientHandler.class
         */
        String socketInfo = initializeServerSocket(socket);

        // Set up json messages builder for client
        JsonMessageBuilder jsonMessageBuilder = new JsonMessageBuilder();

        // Set up json messages parse for client
        JsonMessageParser jsonMessageParser = new JsonMessageParser();

        userSessionApiCaller = new UserSessionApiCaller(SERVER_URL_SESSIONS);
        userSessionApiCaller.createSession(userName, device_info, ipAddress, socketInfo);

        // Start NotificationService to listen for SSE
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                NotificationApiCaller notificationService = new NotificationApiCaller();
                notificationService.listeningNotifyMessageSeen(userName, device_info);
            }
        });

        messageStatusApiCaller = new MessageStatusApiCaller(SERVER_URL_MESSAGES);
        messageHistoryApiCaller = new MessageHistoryApiCaller(SERVER_URL_MESSAGES, jsonMessageParser);
        messageUnreadApiCaller = new MessageUnreadApiCaller(SERVER_URL_MESSAGES, jsonMessageParser);

        messageQueueManager = new MessageQueueManager();

        // Set up IO for client
        IOServerHandle ioServerHandle = new IOServerHandle(this, socket, userName, jsonMessageBuilder, jsonMessageParser, messageStatusApiCaller, messageHistoryApiCaller, messageUnreadApiCaller, messageQueueManager);
        ioServerHandle.initialize();
    }


    public Socket reconnect() {
        Socket socket;
        while (true) {
            try {
                socket = connectServer();
                System.out.println("Reconnect to the server successfully");

                /**
                 * Swap port and local port to socket as server-side socket
                 * to synchronize with SocketManager.class and currentSocket at IOClientHandler.class
                 */
                String socketInfo = initializeServerSocket(socket);

                userSessionApiCaller.createSession(userName, device_info, ipAddress, socketInfo);

                // Start NotificationService to listen for SSE
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        NotificationApiCaller notificationService = new NotificationApiCaller();
                        notificationService.listeningNotifyMessageSeen(userName, device_info);
                    }
                });

                break;

            } catch (ConnectionLostException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        return socket;
    }

    private Socket connectServer() throws ConnectionLostException {
        try {
            return new Socket(SERVER_ADDRESS, SERVER_PORT);
        } catch (IOException e) {
            throw new ConnectionLostException("Reconnecting to the server at " + SERVER_ADDRESS + ":" + SERVER_PORT, e);
        }
    }

    private String initializeServerSocket(Socket socket) {
        // Get information from Socket
        String remoteAddress = socket.getInetAddress().getHostAddress();
        int remotePort = socket.getPort();
        int localPort = socket.getLocalPort();

        String socketInfo = String.format("Socket[addr=/%s,port=%d,localport=%d]",
                remoteAddress, localPort, remotePort); // swap local port and remotePort
        return socketInfo;
    }


}
