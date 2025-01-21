package com.example.Telegram.controller.server.http.restful;

import com.example.Telegram.model.data.Message;
import com.example.Telegram.model.repository.MessageRepository;
import com.example.Telegram.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/messages")
public class MessageStatusController {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserRepository userRepository;


    @PostMapping("/seen")
    public ResponseEntity<Message> setMessageStatusToSeen(@RequestParam Long messageId) {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isPresent()) {
            Message message = optionalMessage.get();
            message.setStatus("seen");
            message.setSeenAt(LocalDateTime.now());
            messageRepository.save(message);
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
