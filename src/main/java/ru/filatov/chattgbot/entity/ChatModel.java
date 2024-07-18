package ru.filatov.chattgbot.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class ChatModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String modelName;

    @Column(nullable = false)
    private int tokenLimitSent;

    @Column(nullable = false)
    private int tokenLimitReceived;

    @Column(nullable = false)
    private int requestLimit;
}
