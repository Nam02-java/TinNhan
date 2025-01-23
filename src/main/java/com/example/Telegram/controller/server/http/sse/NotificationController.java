package com.example.Telegram.controller.server.http.sse;

import com.example.Telegram.model.data.Message;
import com.example.Telegram.model.data.User;
import com.example.Telegram.model.data.UserSession;
import com.example.Telegram.model.repository.MessageRepository;
import com.example.Telegram.model.repository.UserRepository;
import com.example.Telegram.model.repository.UserSessionRepository;
import com.example.Telegram.service.sse.SseEmitterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final ExecutorService executor ;
    private final SseEmitterManager semitterManager;

    @Autowired
    public NotificationController(SseEmitterManager semitterManager) {
        this.semitterManager = semitterManager;
        this.executor = new ThreadPoolExecutor(
                4, // corePoolSize
                4, // maximumPoolSize
                0L, // keepAliveTime
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()
        );
    }

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;


    @GetMapping("/{currentUsername}/{device_info}/listening-message-seen")
    public SseEmitter notifyMessageSeen(@PathVariable String currentUsername,
                                        @PathVariable String device_info) {

        User user = userRepository.findByUsername(currentUsername);
        Long senderId = user.getId();

        String sessionToken = null;
        List<UserSession> userSessionList = userSessionRepository.findByUserId(senderId);

        // Find sessionToken if user_id and device_info match
        for (UserSession session : userSessionList) {
            if (senderId.equals(session.getUserId()) && device_info.equals(session.getDeviceInfo())) {
                sessionToken = session.getSessionToken();
                break;
            }
        }

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        semitterManager.addEmitter(sessionToken, emitter);

        executor.execute(() -> {
            try {
                Long lastMessageId = null;
                while (!Thread.currentThread().isInterrupted()) {
                    Message message = messageRepository.findTopBySenderIdAndStatusSeen(senderId);

                    if (!message.getId().equals(lastMessageId)) {
                        if (!message.getRecipient().getId().equals(senderId)) {
                            String info = message.getMessageContent() + " - seen at " + "[" + message.getSeenAt() + "]";
                            emitter.send(info);
                            lastMessageId = message.getId();
                        }
                    }
                    Thread.sleep(2000);
                }

            } catch (IOException | InterruptedException e) {
                System.out.println(e);
                emitter.completeWithError(e);
            } finally {
                emitter.complete();
            }
        });

        String finalSessionToken = sessionToken;

        emitter.onCompletion(() -> {
            System.out.println("Emitter for " + currentUsername + " with " + finalSessionToken + " completed.");
            semitterManager.removeEmitter(finalSessionToken);
        });
        emitter.onTimeout(() -> {
            System.out.println("Emitter for " + currentUsername + " with " + finalSessionToken + " timed out.");
            semitterManager.removeEmitter(finalSessionToken);
        });
        emitter.onError((e) -> {
            System.err.println("Error in SSE for " + currentUsername + ": " + e.getMessage());
            semitterManager.removeEmitter(finalSessionToken);
        });


        return emitter;
    }
}

/**
 * Continue researching timeout of client's sseEmitter and server's sseEmitter
 * <p>
 * http://localhost:8080/api/notifications/Nam02/listening-message-seen
 * http://localhost:8080/api/notifications/Linh02/listening-message-seen
 * <p>
 * private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
 * private final ExecutorService executor = Executors.newFixedThreadPool(2);
 */

//@PostMapping("/{currentUsername}/{device_info}/stop-sse")
//    public ResponseEntity<String> stopSseForUser(
//            @PathVariable String currentUsername,
//            @PathVariable String device_info) {
//        User user = userRepository.findByUsername(currentUsername);
//        if (user == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
//        }
//
//        emitterManager.removeEmitter(currentUsername);
//        return ResponseEntity.ok("Stopped SSE for user: " + currentUsername + ", client: " + currentUsername);
//    }
