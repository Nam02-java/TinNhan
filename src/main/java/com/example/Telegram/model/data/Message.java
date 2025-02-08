package com.example.Telegram.model.data;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "message_content", nullable = false)
    private String messageContent;

    @Column(name = "sent_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime sentAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "seen_at", nullable = true)
    private LocalDateTime seenAt;

    @Column(name = "seen_notified_status", nullable = false, length = 10)
    private String seenNotifiedStatus;

    @PrePersist
    protected void onCreate() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "unread";
        }
        if (this.seenNotifiedStatus == null) {
            this.seenNotifiedStatus = "unsent";
        }
    }

}
