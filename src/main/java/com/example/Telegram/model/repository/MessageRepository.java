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
    @Query("SELECT MAX(m.id) FROM Message m WHERE m.sender.id = :senderId AND m.seenNotifiedStatus = 'sent'")
    Long findLastSeenMessageId(@Param("senderId") Long senderId);

    //Only retrieve messages that have been seen but not notified
    @Query("SELECT m FROM Message m WHERE m.sender.id = :senderId AND m.status = 'seen' AND m.seenNotifiedStatus = 'unsent' AND m.id > :lastSeenMessageId ORDER BY m.id ASC")
    List<Message> findUnsentMessagesFromId(@Param("senderId") Long senderId, @Param("lastSeenMessageId") Long lastSeenMessageId);


}
