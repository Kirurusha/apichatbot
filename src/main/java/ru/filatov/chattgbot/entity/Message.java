package ru.filatov.chattgbot.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_context_id", nullable = false)
    private ChatContext chatContext;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false, length = 4096)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public Message(String role, String content, LocalDateTime timestamp) {
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Message() {
        // Default constructor
    }
}
