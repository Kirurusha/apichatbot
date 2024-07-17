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
        return subscriptions.get(0); // Assuming the latest subscription is the first one
    }
    public Optional<Subscription> findActiveSubscriptionByUserId(Long userId) {
        return subscriptionRepository.findFirstByUserIdAndEndDateAfterOrderByEndDateDesc(userId, LocalDateTime.now());
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
        freeSubscription.setEndDate(LocalDateTime.now().plusMonths(1)); // Free subscription valid for 1 month
        freeSubscription.setRequestLimit(100); // Example limit
        freeSubscription.setTokenLimitReceived(1000); // Example token limit
        freeSubscription.setTokenLimitSent(1000); // Example token limit
        subscriptionRepository.save(freeSubscription);
        return freeSubscription;
    }

    public boolean isRequestLimitReached(Long userId, String chatVersion) {
        Optional<Subscription> activeSubscriptionOpt = subscriptionRepository.findFirstByUserIdAndEndDateAfterOrderByEndDateDesc(userId, LocalDateTime.now());

        if (activeSubscriptionOpt.isPresent()) {
            Subscription activeSubscription = activeSubscriptionOpt.get();
            int requestLimit = activeSubscription.getRequestLimit();
            int tokenLimitSent = activeSubscription.getTokenLimitSent();
            int tokenLimitReceived = activeSubscription.getTokenLimitReceived();

            int requestsMade = subscriptionRepository.countRequestsMadeInCurrentPeriod(userId, chatVersion, activeSubscription.getStartDate());
            int tokensSent = subscriptionRepository.countTokensSentInCurrentPeriod(userId, chatVersion, activeSubscription.getStartDate());
            int tokensReceived = subscriptionRepository.countTokensReceivedInCurrentPeriod(userId, chatVersion, activeSubscription.getStartDate());

            return requestsMade >= requestLimit || tokensSent >= tokenLimitSent || tokensReceived >= tokenLimitReceived;
        }

        return true;  // Если подписки нет, то лимит запросов считается достигнутым
    }



}
