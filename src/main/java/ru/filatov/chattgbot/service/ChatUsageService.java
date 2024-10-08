package ru.filatov.chattgbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.filatov.chattgbot.entity.ChatUsage;
import ru.filatov.chattgbot.entity.Subscription;
import ru.filatov.chattgbot.entity.User;
import ru.filatov.chattgbot.repository.ChatUsageRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatUsageService {
    @Autowired
    private ChatUsageRepository chatUsageRepository;
    private final SubscriptionService subscriptionService;

    public ChatUsageService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    private static final int BASE_TOKEN_LIMIT_SENT = 1000;
    private static final int BASE_TOKEN_LIMIT_RECEIVED = 1000;
    private static final int BASE_REQUEST_LIMIT = 50;

    public List<ChatUsage> findByUserId(Long userId) {
        return chatUsageRepository.findByUserId(userId);
    }

    public ChatUsage findByUserIdAndChatVersion(Long userId, String chatVersion) {
        return chatUsageRepository.findByUserIdAndChatVersion(userId, chatVersion);
    }

    public void save(ChatUsage chatUsage) {
        chatUsageRepository.save(chatUsage);
    }

    public ChatUsage findByUserIdAndVersion(Long userId, String version) {
        Optional<ChatUsage> chatUsage = Optional.ofNullable(chatUsageRepository.findByUserIdAndChatVersion(userId, version));
        return chatUsage.orElse(null);
    }

    public boolean checkAndUpdateLimits(ChatUsage chatUsage, int tokensSent, int tokensReceived) {
        User user = chatUsage.getUser();
        if (user.isAdmin()) {
            return true; // Админам не применяются лимиты
        }

        Subscription subscription = chatUsage.getSubscription();
        int tokenLimitSent = (subscription != null) ? subscription.getTotalTokenLimitSent() : BASE_TOKEN_LIMIT_SENT;
        int tokenLimitReceived = (subscription != null) ? subscription.getTotalTokenLimitReceived() : BASE_TOKEN_LIMIT_RECEIVED;
        int requestLimit = (subscription != null) ? subscription.getTotalRequestLimit() : BASE_REQUEST_LIMIT;

        if (chatUsage.getTokensSent() + tokensSent > tokenLimitSent ||
                chatUsage.getTokensReceived() + tokensReceived > tokenLimitReceived ||
                chatUsage.getRequestsMade() >= requestLimit) {
            return false;
        }

        chatUsage.setTokensSent(chatUsage.getTokensSent() + tokensSent);
        chatUsage.setTokensReceived(chatUsage.getTokensReceived() + tokensReceived);
        chatUsage.setRequestsMade(chatUsage.getRequestsMade() + 1);
        chatUsageRepository.save(chatUsage);

        return true;
    }

    public String getUserStatistics(Long userId) {
        List<ChatUsage> chatUsages = findByUserId(userId);
        if (chatUsages.isEmpty()) {
            return "No usage data found for user.";
        }

        int totalTokensSent = chatUsages.stream().mapToInt(ChatUsage::getTokensSent).sum();
        int totalTokensReceived = chatUsages.stream().mapToInt(ChatUsage::getTokensReceived).sum();
        int totalRequestsMade = chatUsages.stream().mapToInt(ChatUsage::getRequestsMade).sum();

        // Предположим, что у пользователя может быть подписка на текущую модель
        String modelName = chatUsages.get(0).getChatVersion(); // Предполагаем, что у всех usage одна и та же модель
        Optional<Subscription> activeSubscriptionOpt = subscriptionService.findActiveSubscriptionByUserId(userId, modelName);
        String subscriptionInfo;
        if (activeSubscriptionOpt.isPresent()) {
            Subscription subscription = activeSubscriptionOpt.get();
            subscriptionInfo = String.format("Subscription: %s\nStart Date: %s\nEnd Date: %s\nRequest Limit: %d\nToken Limit Sent: %d\nToken Limit Received: %d\n",
                    subscription.getSubscriptionType(),
                    subscription.getStartDate(),
                    subscription.getEndDate(),
                    subscription.getTotalRequestLimit(),
                    subscription.getTotalTokenLimitSent(),
                    subscription.getTotalTokenLimitReceived());
        } else {
            subscriptionInfo = "Subscription: None\n";
        }

        return String.format("User ID: %d\nTotal Tokens Sent: %d\nTotal Tokens Received: %d\nTotal Requests Made: %d\n%s",
                userId, totalTokensSent, totalTokensReceived, totalRequestsMade, subscriptionInfo);
    }


    public String getAllUsersStatistics() {
        List<ChatUsage> chatUsages = chatUsageRepository.findAll();

        int totalTokensSent = chatUsages.stream().mapToInt(ChatUsage::getTokensSent).sum();
        int totalTokensReceived = chatUsages.stream().mapToInt(ChatUsage::getTokensReceived).sum();
        int totalRequestsMade = chatUsages.stream().mapToInt(ChatUsage::getRequestsMade).sum();

        return String.format("Total Tokens Sent: %d\nTotal Tokens Received: %d\nTotal Requests Made: %d",
                totalTokensSent, totalTokensReceived, totalRequestsMade);
    }

    public String getFullStatistics() {
        List<ChatUsage> chatUsages = chatUsageRepository.findAll();

        Map<User, List<ChatUsage>> usageByUser = chatUsages.stream()
                .collect(Collectors.groupingBy(ChatUsage::getUser));

        StringBuilder statsBuilder = new StringBuilder();

        statsBuilder.append("Overall Statistics:\n");
        statsBuilder.append(getAllUsersStatistics()).append("\n");

        for (Map.Entry<User, List<ChatUsage>> entry : usageByUser.entrySet()) {
            User user = entry.getKey();
            List<ChatUsage> usages = entry.getValue();

            statsBuilder.append(String.format("\nUser: %s (ID: %d) (Telegram_ID: %d)\n", user.getUsername(), user.getId(), user.getTelegramId()));

            // Получаем все активные подписки пользователя
            List<Subscription> subscriptions = subscriptionService.findByUserId(user.getId());
            if (!subscriptions.isEmpty()) {
                for (Subscription subscription : subscriptions) {
                    statsBuilder.append(String.format("Subscription: %s\nModel: %s\nStart Date: %s\nEnd Date: %s\nRequest Limit: %d\nToken Limit Sent: %d\nToken Limit Received: %d\n",
                            subscription.getSubscriptionType(),
                            subscription.getModelName(),
                            subscription.getStartDate(),
                            subscription.getEndDate(),
                            subscription.getTotalRequestLimit(),
                            subscription.getTotalTokenLimitSent(),
                            subscription.getTotalTokenLimitReceived()));
                }
            } else {
                statsBuilder.append("Subscription: None\n");
            }

            Map<String, List<ChatUsage>> usageByVersion = usages.stream()
                    .collect(Collectors.groupingBy(ChatUsage::getChatVersion));

            for (Map.Entry<String, List<ChatUsage>> versionEntry : usageByVersion.entrySet()) {
                String version = versionEntry.getKey();
                List<ChatUsage> versionUsages = versionEntry.getValue();

                int totalTokensSent = versionUsages.stream().mapToInt(ChatUsage::getTokensSent).sum();
                int totalTokensReceived = versionUsages.stream().mapToInt(ChatUsage::getTokensReceived).sum();
                int totalRequestsMade = versionUsages.stream().mapToInt(ChatUsage::getRequestsMade).sum();

                statsBuilder.append(String.format("Version: %s\nTotal Tokens Sent: %d\nTotal Tokens Received: %d\nTotal Requests Made: %d\n",
                        version, totalTokensSent, totalTokensReceived, totalRequestsMade));
            }
        }

        return statsBuilder.toString();
    }

}
