package com.example.Telegram.controller.server;

import com.example.Telegram.exception.ConnectionLostException;
import com.example.Telegram.model.data.Message;
import com.example.Telegram.model.data.SocketConnection;
import com.example.Telegram.model.data.User;
import com.example.Telegram.model.data.UserSession;
import com.example.Telegram.model.repository.MessageRepository;
import com.example.Telegram.model.repository.SocketConnectionRepository;
import com.example.Telegram.model.repository.UserRepository;
import com.example.Telegram.model.repository.UserSessionRepository;
import com.example.Telegram.service.json.JsonMessageParser;
import com.example.Telegram.service.json.JsonMessageUpdate;
import com.example.Telegram.service.socket.SocketManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.System.out;

public class IOClientHandler implements Runnable {
    private final SocketManager socketManager;
    private final Socket currentSocket;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final UserSessionRepository userSessionRepository;
    private final SocketConnectionRepository socketConnectionRepository;
    private BufferedReader inputFromClient;
    private DataOutputStream outputToClient;
    private JsonMessageParser jsonMessageParser;
    private JsonMessageUpdate jsonMessageUpdate;
    private String messageFromClient;

    public IOClientHandler(SocketManager socketManager,
                           Socket socket,
                           UserRepository userRepository,
                           MessageRepository messageRepository,
                           UserSessionRepository userSessionRepository,
                           SocketConnectionRepository socketConnectionRepository,
                           JsonMessageParser jsonMessageParser,
                           JsonMessageUpdate jsonMessageUpdate) {
        this.socketManager = socketManager;
        this.currentSocket = socket;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.userSessionRepository = userSessionRepository;
        this.socketConnectionRepository = socketConnectionRepository;
        this.jsonMessageParser = jsonMessageParser;
        this.jsonMessageUpdate = jsonMessageUpdate;
    }

    @Override
    public void run() {



        try {
            inputFromClient = new BufferedReader(new InputStreamReader(currentSocket.getInputStream()));

            while ((messageFromClient = inputFromClient.readLine()) != null) {

                System.out.println("Received: " + messageFromClient);

                jsonMessageParser.parseJson(messageFromClient);
                String messageContent = jsonMessageParser.getMessage();
                LocalDateTime sentAt = jsonMessageParser.getSentAt();

                // Based on the socket, find the session_id
                SocketConnection senderSocketConnection = socketConnectionRepository.findBySocketInfo(currentSocket.toString());

                // Based on session_id, find user_id
                Optional<UserSession> optionalUserSession = userSessionRepository.findById(senderSocketConnection.getSessionId());
                UserSession senderUserSession = optionalUserSession.get();

                // Based on user_id, find the sender
                Optional<User> optionalUser = userRepository.findById(senderUserSession.getUserId());
                User sender = optionalUser.get();

                // Find the recipient (the other user in the database)
                List<User> allUsers = userRepository.findAll();
                User recipient = null;
                for (User otherUser : allUsers) {
                    if (!otherUser.getUsername().equals(sender.getUsername())) {
                        recipient = otherUser;
                        break;
                    }
                }

                // Save the message in the database before broadcast to other clients
                if (recipient != null) {
                    Message message = new Message();
                    message.setSender(sender);
                    message.setRecipient(recipient);
                    message.setMessageContent(messageContent);
                    message.setSentAt(sentAt);
                    messageRepository.save(message);
                }

                // Broadcast to other clients
                Message lastMessage = messageRepository.findTop1ByOrderByIdDesc();
                Long messageId = lastMessage.getId();

                // Add id field for json
                messageFromClient = jsonMessageUpdate.addField(messageFromClient, "id", messageId);

                for (Socket targetSocket : socketManager.getClientSocketsList()) {
                    if (targetSocket != currentSocket) {
                        outputToClient = new DataOutputStream(targetSocket.getOutputStream());
                        outputToClient.writeBytes(messageFromClient + "\n");
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Client disconnected: " + currentSocket.getInetAddress());

        } finally {

            // Based on the socket, find socket record
            SocketConnection senderSocketConnection = socketConnectionRepository.findBySocketInfo(currentSocket.toString());

            // Based on session_id, find user session record
            Optional<UserSession> optionalUserSession = userSessionRepository.findById(senderSocketConnection.getSessionId());
            UserSession senderUserSession = optionalUserSession.get();

            // SocktManager remove current socket from list
            socketManager.removeClient(currentSocket);

            // Socket Connection delete socket in database
            socketConnectionRepository.deleteById(senderSocketConnection.getId());

            out.println("up con cu may date di");
            // User Sessions set status online to offline in database
            userSessionRepository.updateStatusToOfflineById(senderUserSession.getId());

            try {
                currentSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

