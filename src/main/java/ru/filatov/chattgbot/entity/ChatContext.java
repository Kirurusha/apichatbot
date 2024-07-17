package ru.filatov.chattgbot.entity;

import lombok.Data;
import jakarta.persistence.*;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Entity
@Data
public class ChatContext {
    private static final int MAX_TOKENS = 1200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(nullable = false)
    private int currentTokenCount;

    @OneToMany(mappedBy = "chatContext", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Message> messages;

    public ChatContext() {
        this.currentTokenCount = 0;
        this.messages = new LinkedList<>();
    }

    public void updateUserMessages(String role, String content) {
        addMessage(content, role);
    }

    public void addMessage(String messageContent, String role) {
        int messageTokens = countTokens(messageContent);
        LocalDateTime now = LocalDateTime.now();

        // Удаление сообщений старше 8 часов
        while (!messages.isEmpty() && Duration.between(messages.get(0).getTimestamp(), now).toHours() > 8) {
            Message removedMessage = messages.remove(0);
            currentTokenCount -= countTokens(removedMessage.getContent());
        }

        // Удаление старых сообщений, чтобы оставить место для новых
        while (currentTokenCount + messageTokens > MAX_TOKENS) {
            Message removedMessage = messages.remove(0);
            currentTokenCount -= countTokens(removedMessage.getContent());
        }

        Message newMessage = new Message(role, messageContent, now);
        newMessage.setChatContext(this);
        messages.add(newMessage);
        currentTokenCount += messageTokens;
    }

    private int countTokens(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        return content.split("[\\s\\p{Punct}]+").length;
    }
}
