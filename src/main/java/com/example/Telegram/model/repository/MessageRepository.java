package com.example.Telegram.model.repository;

import com.example.Telegram.model.data.Message;
import com.example.Telegram.model.data.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Message findTop1ByOrderByIdDesc();
    Page<Message> findAllByOrderByIdDesc(Pageable pageable);
    List<Message> findByRecipientIdAndStatus(Long recipientId, String status);  // Find recipient messages by ID and status
    @Query(value = "SELECT * FROM messages WHERE sender_id = :senderId AND status = 'seen' ORDER BY seen_at DESC LIMIT 1", nativeQuery = true)
    Message findTopBySenderIdAndStatusSeen(@Param("senderId") Long senderId);

}
