package com.example.Telegram.controller.server.http.restful;

import com.example.Telegram.model.data.Message;
import com.example.Telegram.model.repository.MessageRepository;
import com.example.Telegram.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
public class MessageHistoryController {

    @Autowired
    private MessageRepository messageRepository;


    @GetMapping("/history")
    public ResponseEntity<Page<Message>> getMessageHistory(
            @RequestParam(value = "currentPage", defaultValue = "0") int currentPage,
            @RequestParam(value = "pageSize", defaultValue = "5") int pageSize) {

        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Message> messages = messageRepository.findAllByOrderByIdDesc(pageable);

        return ResponseEntity.ok(messages);
    }
}
