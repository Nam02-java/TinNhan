package com.example.Telegram;

import com.example.Telegram.model.repository.SocketConnectionRepository;
import com.example.Telegram.model.repository.UserRepository;
import com.example.Telegram.model.repository.UserSessionRepository;
import com.example.Telegram.service.socket.ServerManager;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ServerShutdownHook {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserSessionRepository userSessionRepository;
    @Autowired
    private SocketConnectionRepository socketConnectionRepository;
    @Autowired
    private ServerManager serverManager;

    @PreDestroy
    @Transactional
    public void onShutdown() {
        System.out.println("Destroy method in TelegramApplication invoked");

        // Update all statuses of user session records to offline
        try {
            userSessionRepository.updateAllStatusToOffline();
            System.out.println("All user session statuses set to offline");
        } catch (Exception e) {
            System.err.println("Error updating user session statuses: " + e.getMessage());
        }

        // Delete all socket connection records
        try {
            socketConnectionRepository.deleteAll();
            System.out.println("All socket connection records deleted");
        } catch (Exception e) {
            System.err.println("Error deleting socket connection records: " + e.getMessage());
        }
    }
}
