package ru.filatov.chattgbot.repository;

import ru.filatov.chattgbot.entity.ChatUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatUsageRepository extends JpaRepository<ChatUsage, Long> {
    List<ChatUsage> findByUserId(Long userId);
    ChatUsage findByUserIdAndChatVersion(Long userId, String chatVersion);
}
