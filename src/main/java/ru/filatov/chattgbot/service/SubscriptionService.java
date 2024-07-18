package ru.filatov.chattgbot.service;

import org.springframework.stereotype.Service;
import ru.filatov.chattgbot.entity.Subscription;
import ru.filatov.chattgbot.entity.User;
import ru.filatov.chattgbot.repository.SubscriptionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription getCurrentSubscription(User user) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserIdAndEndDateAfter(user.getId(), LocalDateTime.now());
        if (subscriptions.isEmpty()) {
            return null;
        }
        return subscriptions.get(0); // Предполагаем, что последняя подписка - первая в списке
    }

    public Optional<Subscription> findActiveSubscriptionByUserId(Long userId, String modelName) {
        return subscriptionRepository.findFirstByUserIdAndEndDateAfterAndModelNameOrderByEndDateDesc(userId, LocalDateTime.now(), modelName);
    }

    public List<Subscription> findByUserId(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    public void save(Subscription subscription) {
        subscriptionRepository.save(subscription);
    }

    public Subscription createFreeSubscriptionForUser(User user) {
        Subscription freeSubscription = new Subscription();
        freeSubscription.setUser(user);
        freeSubscription.setSubscriptionType("FREE");
        freeSubscription.setStartDate(LocalDateTime.now());
        freeSubscription.setEndDate(LocalDateTime.now().plusMonths(1)); // Бесплатная подписка на 1 месяц
        freeSubscription.setTotalTokenLimitSent(1000); // Пример лимита токенов
        freeSubscription.setTotalTokenLimitReceived(1000); // Пример лимита токенов
        freeSubscription.setTotalRequestLimit(100); // Пример лимита запросов
        freeSubscription.setModelName("gpt-3.5-turbo-0125"); // Установите значение по умолчанию для модели
        subscriptionRepository.save(freeSubscription);
        return freeSubscription;
    }

    public Subscription createGpt4SubscriptionForUser(User user) {
        Subscription gpt4Subscription = new Subscription();
        gpt4Subscription.setUser(user);
        gpt4Subscription.setSubscriptionType("FREE");
        gpt4Subscription.setStartDate(LocalDateTime.now());
        gpt4Subscription.setEndDate(LocalDateTime.now().plusMonths(1)); // Бесплатная подписка на 1 месяц
        gpt4Subscription.setTotalTokenLimitSent(1000); // Пример лимита токенов
        gpt4Subscription.setTotalTokenLimitReceived(1000); // Пример лимита токенов
        gpt4Subscription.setTotalRequestLimit(100); // Пример лимита запросов
        gpt4Subscription.setModelName("gpt-4"); // Устанавливаем значение модели на "gpt-4"
        subscriptionRepository.save(gpt4Subscription);
        return gpt4Subscription;
    }



    public void extendSubscription(User user, String subscriptionType, int additionalDays, int additionalTokenLimitSent, int additionalTokenLimitReceived, int additionalRequestLimit, String modelName) {
        Optional<Subscription> activeSubscriptionOpt = findActiveSubscriptionByUserId(user.getId(), modelName);

        if (activeSubscriptionOpt.isPresent()) {
            Subscription activeSubscription = activeSubscriptionOpt.get();
            activeSubscription.setEndDate(activeSubscription.getEndDate().plusDays(additionalDays));
            activeSubscription.setTotalTokenLimitSent(activeSubscription.getTotalTokenLimitSent() + additionalTokenLimitSent);
            activeSubscription.setTotalTokenLimitReceived(activeSubscription.getTotalTokenLimitReceived() + additionalTokenLimitReceived);
            activeSubscription.setTotalRequestLimit(activeSubscription.getTotalRequestLimit() + additionalRequestLimit);
            save(activeSubscription);
        } else {
            Subscription newSubscription = new Subscription();
            newSubscription.setUser(user);
            newSubscription.setSubscriptionType(subscriptionType);
            newSubscription.setStartDate(LocalDateTime.now());
            newSubscription.setEndDate(LocalDateTime.now().plusDays(additionalDays));
            newSubscription.setTotalTokenLimitSent(additionalTokenLimitSent);
            newSubscription.setTotalTokenLimitReceived(additionalTokenLimitReceived);
            newSubscription.setTotalRequestLimit(additionalRequestLimit);
            newSubscription.setModelName(modelName);
            save(newSubscription);
        }
    }

    public boolean isRequestLimitReached(Long userId, String modelName) {
        Optional<Subscription> activeSubscriptionOpt = subscriptionRepository.findFirstByUserIdAndEndDateAfterAndModelNameOrderByEndDateDesc(userId, LocalDateTime.now(), modelName);

        if (activeSubscriptionOpt.isPresent()) {
            Subscription activeSubscription = activeSubscriptionOpt.get();
            int requestLimit = activeSubscription.getTotalRequestLimit();
            int requestsMade = subscriptionRepository.countRequestsMadeInCurrentPeriod(userId, activeSubscription.getStartDate());

            return requestsMade >= requestLimit;
        }

        return true;  // Если подписки нет, то лимит запросов считается достигнутым
    }

    public boolean areTokenLimitsReached(Long userId, int tokensSent, int tokensReceived, String modelName) {
        Optional<Subscription> activeSubscriptionOpt = subscriptionRepository.findFirstByUserIdAndEndDateAfterAndModelNameOrderByEndDateDesc(userId, LocalDateTime.now(), modelName);

        if (activeSubscriptionOpt.isPresent()) {
            Subscription activeSubscription = activeSubscriptionOpt.get();
            return tokensSent > activeSubscription.getTotalTokenLimitSent() || tokensReceived > activeSubscription.getTotalTokenLimitReceived();
        }

        return true;  // Если подписки нет, то лимит токенов считается достигнутым
    }
}
