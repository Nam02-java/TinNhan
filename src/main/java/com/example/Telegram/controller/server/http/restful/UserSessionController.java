package com.example.Telegram.controller.server.http.restful;

import com.example.Telegram.model.data.SocketConnection;
import com.example.Telegram.model.data.User;
import com.example.Telegram.model.data.UserSession;
import com.example.Telegram.model.dto.SessionRequestDTO;
import com.example.Telegram.model.repository.SocketConnectionRepository;
import com.example.Telegram.model.repository.UserRepository;
import com.example.Telegram.model.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
public class UserSessionController {

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private SocketConnectionRepository socketConnectionRepository;
    
    @Autowired
    private UserRepository userRepository;


    @PostMapping("/create")
    public ResponseEntity<UserSession> createSession(@RequestBody SessionRequestDTO request) {

        UserSession userSession = new UserSession();

        SocketConnection socketConnection = new SocketConnection();
        socketConnection.setSocketInfo(String.valueOf(request.getSocket()));
        socketConnection.setCreatedAt(LocalDateTime.now());

        User user = userRepository.findByUsername(request.getUsername());

        // Create session Token from combined information
        String tokenSource = request.getDeviceInfo() + request.getUsername();
        String sessionToken = UUID.nameUUIDFromBytes(tokenSource.getBytes()).toString();

        // Check if the sessionToken already exists
        UserSession existingSession = userSessionRepository.findBySessionToken(sessionToken);

        if (existingSession != null) {
            // If the session token exists, update the existing session
            existingSession.setLastActiveAt(LocalDateTime.now());
            existingSession.setStatus(UserSession.Status.online);

            userSessionRepository.save(existingSession);

            // Save socket connection info
            socketConnection.setSessionId(existingSession.getId());
            socketConnectionRepository.save(socketConnection);


            return ResponseEntity.ok(existingSession);


        } else {
            // If no session exists, create a new session
            userSession.setUserId(user.getId());
            userSession.setDeviceInfo(request.getDeviceInfo());
            userSession.setIpAddress(request.getIpAddress());
            userSession.setStatus(UserSession.Status.online);
            userSession.setCreatedAt(LocalDateTime.now());
            userSession.setLastActiveAt(LocalDateTime.now());
            userSession.setSessionToken(sessionToken);
            userSessionRepository.save(userSession);

            // Save socket connection info
            socketConnection.setSessionId(userSession.getId());
            socketConnectionRepository.save(socketConnection);

            return ResponseEntity.ok(userSession);

        }
    }
}

