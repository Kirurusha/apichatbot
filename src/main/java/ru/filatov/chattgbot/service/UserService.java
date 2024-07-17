package ru.filatov.chattgbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.filatov.chattgbot.entity.User;
import ru.filatov.chattgbot.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public Optional<User> findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    public boolean isAdmin(Long telegramId) {
        return findByTelegramId(telegramId)
                .map(User::isAdmin)
                .orElse(false);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
    public String getUserModel(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .map(User::getModel)
                .orElse("gpt-3.5-turbo");
    }

    public void setUserModel(Long telegramId, String model) {
        Optional<User> userOpt = userRepository.findByTelegramId(telegramId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setModel(model);
            userRepository.save(user);
        }
    }

}