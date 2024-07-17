package ru.filatov.chattgbot.service;

import ru.filatov.chattgbot.entity.Message;
import ru.filatov.chattgbot.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    public void save(Message message) {
        messageRepository.save(message);
    }

    public void delete(Message message) {
        messageRepository.delete(message);
    }
}
