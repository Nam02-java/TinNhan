package com.example.Telegram;

import com.example.Telegram.model.repository.SocketConnectionRepository;
import com.example.Telegram.model.repository.UserSessionRepository;
import com.example.Telegram.service.socket.ServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class TelegramApplication {

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private SocketConnectionRepository socketConnectionRepository;

    public static void main(String[] args) throws IOException {
        ApplicationContext context = SpringApplication.run(TelegramApplication.class, args);
        ServerManager serverManager = context.getBean(ServerManager.class);
        serverManager.startServer();
    }
}

