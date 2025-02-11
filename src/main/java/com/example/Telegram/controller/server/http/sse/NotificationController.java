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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final ExecutorService executor;
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


    private final Map<String, Future<?>> emitterTasks = new ConcurrentHashMap<>();

    @GetMapping("/{currentUsername}/{device_info}/listening-message-seen")
    public SseEmitter notifyMessageSeen(@PathVariable String currentUsername,
                                        @PathVariable String device_info) {

        User user = userRepository.findByUsername(currentUsername);
        Long senderId = user.getId();

        String sessionToken = null;
        List<UserSession> userSessionList = userSessionRepository.findByUserId(senderId);
        for (int i = 0; i < userSessionList.size(); i++) {
            UserSession session = userSessionList.get(i);
            if (session.getUserId().equals(senderId) && session.getDeviceInfo().equals(device_info)) {
                sessionToken = session.getSessionToken();
                break;
            }
        }

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        semitterManager.addEmitter(sessionToken, emitter);

        Future<?> future = executor.submit(() -> {
            try {
                Long lastMessageId = null;
                Long lastSeenMessageId = messageRepository.findLastSeenMessageId(senderId);

                while (!Thread.currentThread().isInterrupted()) {
                    // Only retrieve messages that have been seen but not notified
                    List<Message> unsentMessages = messageRepository.findUnsentMessagesFromId(senderId, lastSeenMessageId);
                    if (!unsentMessages.isEmpty()) {
                        for (Message message : unsentMessages) {
                            String info = message.getMessageContent() + " - seen at [" + message.getSeenAt() + "]";
                            emitter.send(info);

                            message.setSeenNotifiedStatus("sent"); // Default value
                            messageRepository.save(message);
                        }

                        // Update new milestones
                        lastSeenMessageId = unsentMessages.get(unsentMessages.size() - 1).getId();
                    }

                    Message lastMessage = messageRepository.findTopBySenderIdAndStatusSeen(senderId);
                    if (lastMessage != null &&
                            !lastMessage.getId().equals(lastMessageId) &&
                            !lastMessage.getId().equals(lastSeenMessageId)) {
                        if (!lastMessage.getRecipient().getId().equals(senderId)) {
                            String info = lastMessage.getMessageContent() + " - seen at " + "[" + lastMessage.getSeenAt() + "]";
                            emitter.send(info);
                            lastMessageId = lastMessage.getId();
                        }
                    }

                    // Rest 200ms to avoid spamming CPU when there is no new news
                    Thread.sleep(200);
                }
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                emitter.completeWithError(e);
            } finally {
                Thread.currentThread().interrupt();
                emitter.complete();
            }
        });


        emitterTasks.put(sessionToken, future);

        final String finalSessionToken = sessionToken;
        emitter.onCompletion(new Runnable() {
            @Override
            public void run() {
                cleanupEmitter(finalSessionToken);
            }
        });
        emitter.onTimeout(new Runnable() {
            @Override
            public void run() {
                cleanupEmitter(finalSessionToken);
            }
        });
        emitter.onError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) {
                cleanupEmitter(finalSessionToken);
            }
        });

        return emitter;
    }

    // Cancel the task when the emitter ends ( complete , completeWithError )
    private void cleanupEmitter(String sessionToken) {
        Future<?> future = emitterTasks.remove(sessionToken);
        if (future != null) {
            future.cancel(true); // Sends a cancel signal and stops the stream
        }
        semitterManager.removeEmitter(sessionToken);
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
