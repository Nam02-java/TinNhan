package com.example.Telegram.controller.server.http.restful;

import com.example.Telegram.model.data.Message;
import com.example.Telegram.model.data.User;
import com.example.Telegram.model.repository.MessageRepository;
import com.example.Telegram.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageUnreadController {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/downloadUnreadMessages")
    public ResponseEntity<List<Message>> downloadUnreadMessages(@RequestBody String username) {
        User recipient = userRepository.findByUsername(username);

        if (recipient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Find all messages that have not been marked as "unread" for a recipient based on recipient_id column
        List<Message> unreadMessages = messageRepository.findByRecipientIdAndStatus(recipient.getId(), "unread");

        if (unreadMessages.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        // Set all message unread to seen
        for (Message message : unreadMessages) {
            message.setStatus("seen");
            message.setSeenAt(LocalDateTime.now());
            messageRepository.save(message);
        }

        return ResponseEntity.ok(unreadMessages);

    }
}
