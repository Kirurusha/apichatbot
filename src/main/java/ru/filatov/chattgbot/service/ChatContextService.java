package ru.filatov.chattgbot.service;

import jakarta.transaction.Transactional;
import ru.filatov.chattgbot.entity.ChatContext;
import ru.filatov.chattgbot.repository.ChatContextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatContextService {
    @Autowired
    private ChatContextRepository chatContextRepository;

    public ChatContext findByUserId(Long userId) {
        return chatContextRepository.findByUserId(userId);
    }

    public void save(ChatContext chatContext) {
        chatContextRepository.save(chatContext);
    }

    public void delete(ChatContext chatContext) {
        chatContextRepository.delete(chatContext);
    }
    @Transactional
    public void clearChatContext(Long userId) {
        ChatContext chatContext = findByUserId(userId);
        if (chatContext != null) {
            chatContext.getMessages().clear();
            chatContext.setCurrentTokenCount(0);
            save(chatContext);
        }
    }
}
