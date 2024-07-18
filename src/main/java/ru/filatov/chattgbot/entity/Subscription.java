package ru.filatov.chattgbot.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
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

    @Getter
    @Setter
    @Column(nullable = false)
    private int totalTokenLimitSent;

    @Getter
    @Setter
    @Column(nullable = false)
    private int totalTokenLimitReceived;

    @Getter
    @Setter
    @Column(nullable = false)
    private int totalRequestLimit;

    @Getter
    @Setter
    @Column(nullable = false)
    private String modelName;

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

    public void extendSubscription(int additionalDays, int additionalTokenLimitSent, int additionalTokenLimitReceived, int additionalRequestLimit) {
        this.endDate = this.endDate.plusDays(additionalDays);
        this.totalTokenLimitSent += additionalTokenLimitSent;
        this.totalTokenLimitReceived += additionalTokenLimitReceived;
        this.totalRequestLimit += additionalRequestLimit;
    }
}
