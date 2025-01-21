package com.example.Telegram.controller.server.http.sse;

import com.example.Telegram.model.data.Message;
import com.example.Telegram.model.data.User;
import com.example.Telegram.model.repository.MessageRepository;
import com.example.Telegram.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {


    /**
     * Continue researching timeout of client's sseEmitter and server's sseEmitter
     */

    /**
     * http://localhost:8080/api/notifications/Nam02/listening-message-seen
     * http://localhost:8080/api/notifications/Linh02/listening-message-seen
     */

    /**
     * private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
     * private final ExecutorService executor = Executors.newFixedThreadPool(2);
     */

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{currentUsername}/listening-message-seen")
    public SseEmitter notifyMessageSeen(@PathVariable String currentUsername) {

        User user = userRepository.findByUsername(currentUsername);
        Long senderId = user.getId();

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
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

        emitter.onCompletion(() -> {
            System.out.println("Emitter for " + currentUsername + " completed.");
        });
        emitter.onTimeout(() -> {
            System.out.println("Emitter for " + currentUsername + " timed out.");
        });
        emitter.onError((e) -> {
            System.err.println("Error in SSE for " + currentUsername + ": " + e.getMessage());
        });

        return emitter;
    }
}

