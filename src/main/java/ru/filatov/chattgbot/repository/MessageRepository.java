package ru.filatov.chattgbot.repository;

import ru.filatov.chattgbot.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {}
