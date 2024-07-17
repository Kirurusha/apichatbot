package ru.filatov.chattgbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.filatov.chattgbot.entity.User;
import ru.filatov.chattgbot.repository.UserRepository;

import java.util.Optional;

@Service
public class AdminService {
    @Autowired
    private UserRepository userRepository;

    public void blockUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setActive(false);
            userRepository.save(user);
        }
    }

    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setActive(true);
            userRepository.save(user);
        }
    }
    public boolean isAdmin(Long telegramId) {
        Optional<User> userOpt = userRepository.findByTelegramId(telegramId);
        return userOpt.isPresent() && userOpt.get().getRole().equalsIgnoreCase("ADMIN");
    }

    public void setTokenLimit(Long userId, int tokenLimitSent, int tokenLimitReceived) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.getSubscriptions().forEach(subscription -> {
                subscription.setTokenLimitSent(tokenLimitSent);
                subscription.setTokenLimitReceived(tokenLimitReceived);
            });
            userRepository.save(user);
        }
    }

    public void setRequestLimit(Long userId, int requestLimit) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.getSubscriptions().forEach(subscription -> {
                subscription.setRequestLimit(requestLimit);
            });
            userRepository.save(user);
        }
    }

    // Новые методы для установки и снятия админских прав
    public void grantAdminRights(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setAdmin(true);
            userRepository.save(user);
        }
    }

    public void revokeAdminRights(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setAdmin(false);
            userRepository.save(user);
        }
    }
}
