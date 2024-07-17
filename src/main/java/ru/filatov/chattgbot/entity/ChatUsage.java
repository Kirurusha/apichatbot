package ru.filatov.chattgbot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
public class ChatUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    @ToString.Exclude
    private Subscription subscription;

    @Column(nullable = false)
    private String chatVersion;

    @Column(nullable = false)
    private int tokensSent;

    @Column(nullable = false)
    private int tokensReceived;

    @Column(nullable = false)
    private int requestsMade;

    @Column(nullable = false)
    private LocalDateTime lastActivity;

    public ChatUsage() {
        this.tokensSent = 0;
        this.tokensReceived = 0;
        this.requestsMade = 0;
        this.lastActivity = LocalDateTime.now();
    }
    public void incrementTokensSent(int tokens) {
        this.tokensSent += tokens;
    }

    public void incrementTokensReceived(int tokens) {
        this.tokensReceived += tokens;
    }

    public void incrementRequestsMade() {
        this.requestsMade++;
    }
}
