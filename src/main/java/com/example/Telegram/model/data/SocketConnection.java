package com.example.Telegram.model.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "socket_connections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocketConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;  // Chỉ lưu session_id thay vì liên kết đối tượng

    @Column(name = "socket_info", columnDefinition = "TEXT")
    private String socketInfo;  // Lưu trữ thông tin về socket

    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
