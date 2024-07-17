package ru.filatov.chattgbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.filatov.chattgbot.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(Long telegramId);
}
