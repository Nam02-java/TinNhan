package com.example.Telegram.service.socket;


import java.net.Socket;
import java.util.*;
import java.util.Collections;

public class SocketManager {

    private List<Socket> clientSocketsList = new ArrayList<>();

    // Add new socket
    public void addClient(Socket socket) {
        clientSocketsList.add(socket);
    }

    // Delete socket
    public void removeClient(Socket socket) {
        clientSocketsList.remove(socket);
    }

    // Get all socket from list
    public synchronized List<Socket> getClientSocketsList() {
        return Collections.unmodifiableList(clientSocketsList);
    }

    // Clear all socket from list
    public void clear() {
        clientSocketsList.clear();
    }
}

