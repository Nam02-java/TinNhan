package com.example.Telegram.service.socket;

import com.example.Telegram.controller.server.IOClientHandler;
import com.example.Telegram.model.repository.MessageRepository;
import com.example.Telegram.model.repository.SocketConnectionRepository;
import com.example.Telegram.model.repository.UserRepository;
import com.example.Telegram.model.repository.UserSessionRepository;
import com.example.Telegram.service.json.JsonMessageParser;
import com.example.Telegram.service.json.JsonMessageUpdate;
import com.example.Telegram.service.sse.SseEmitterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ServerManager {
    private final int PORT = 8081;

    /**
     * Using a thread pool to optimize resources and improve performance, instead of creating a new thread for each client connection
     * This approach allows for better scalability and resource management when handling multiple client connections.
     */
    private final ExecutorService threadPool = Executors.newFixedThreadPool(20);

    /**
     * Single shared socketManager instance
     */
    private SocketManager socketManager;

    private ServerSocket serverSocket;
    private Socket clientSocket;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private SocketConnectionRepository socketConnectionRepository;
    @Autowired
    private SseEmitterManager sseEmitterManager;

    public void startServer() throws IOException {

        serverSocket = new ServerSocket(PORT);
        socketManager = new SocketManager();

        while (true) {

            clientSocket = serverSocket.accept();

            socketManager.addClient(clientSocket);

            JsonMessageParser jsonMessageParser = new JsonMessageParser();
            JsonMessageUpdate jsonMessageUpdate = new JsonMessageUpdate();


            threadPool.submit(new IOClientHandler(
                    socketManager, clientSocket,
                    userRepository, messageRepository, userSessionRepository, socketConnectionRepository,
                    jsonMessageParser, jsonMessageUpdate,
                    sseEmitterManager));


        }
    }
}

