package ru.filatov.chattgbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.filatov.chattgbot.entity.ChatModel;

import java.util.Optional;

public interface ChatModelRepository extends JpaRepository<ChatModel, Long> {
    Optional<ChatModel> findByModelName(String modelName);
}
