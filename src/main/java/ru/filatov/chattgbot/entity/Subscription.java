package ru.filatov.chattgbot.entity;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String subscriptionType;

    @Column(nullable = false)
    private int tokenLimitSent;

    @Column(nullable = false)
    private int tokenLimitReceived;

    @Column(nullable = false)
    private int requestLimit;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    public Subscription() {
        this.startDate = LocalDateTime.now();
        this.endDate = LocalDateTime.now().plusMonths(1); // Пример: подписка на 1 месяц
    }

    public boolean isActive() {
        return LocalDateTime.now().isBefore(endDate);
    }
}
