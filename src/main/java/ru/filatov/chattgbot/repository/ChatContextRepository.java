package ru.filatov.chattgbot.repository;

import ru.filatov.chattgbot.entity.ChatContext;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatContextRepository extends JpaRepository<ChatContext, Long> {
    ChatContext findByUserId(Long userId);
}
