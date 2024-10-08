package ru.filatov.chattgbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.filatov.chattgbot.entity.*;
import ru.filatov.chattgbot.service.*;
import ru.filatov.chattgbot.utils.MessageUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ChatGPTBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;
    private final String openAiApiKey;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final ChatContextService chatContextService;
    private final ChatUsageService chatUsageService;
    private final CommandHandler commandHandler;

    public ChatGPTBot(
            String botToken,
            String botUsername,
            String openAiApiKey,
            UserService userService,
            SubscriptionService subscriptionService,
            ChatContextService chatContextService,
            ChatUsageService chatUsageService,
            CommandHandler commandHandler
    ) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.openAiApiKey = openAiApiKey;
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.chatContextService = chatContextService;
        this.chatUsageService = chatUsageService;
        this.commandHandler = commandHandler;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String userMessage = message.getText();
            String chatId = message.getChatId().toString();
            String username = message.getFrom().getUserName();
            Long telegramId = message.getFrom().getId();

            log.info("Received message from {}: {}", username, userMessage);

            Optional<User> optionalUser = userService.findByTelegramId(telegramId);
            User user = optionalUser.orElseGet(() -> {
                User newUser = new User();
                newUser.setTelegramId(telegramId);
                newUser.setUsername(username);
                newUser.setCreatedAt(LocalDateTime.now());
                newUser.setUpdatedAt(LocalDateTime.now());
                newUser.setModel("gpt-3.5-turbo-0125");
                newUser.setActive(true);
                newUser.setRole("USER");
                newUser.setAdmin(false);
                userService.save(newUser);
                log.info("New user created: {}", username);
                return newUser;
            });

            Optional<ChatContext> optionalChatContext = Optional.ofNullable(chatContextService.findByUserId(user.getId()));
            ChatContext chatContext = optionalChatContext.orElseGet(() -> {
                ChatContext newChatContext = new ChatContext();
                newChatContext.setUser(user);
                newChatContext.setCurrentTokenCount(0);
                chatContextService.save(newChatContext);
                log.info("New chat context created for user: {}", username);
                return newChatContext;
            });

            Optional<Subscription> optionalSubscription = subscriptionService.findActiveSubscriptionByUserId(user.getId(),user.getModel());
            Subscription subscription = optionalSubscription.orElseGet(() -> {
                Subscription freeSubscription = subscriptionService.createFreeSubscriptionForUser(user);
                log.info("Free subscription created for user: {}", username);
                return freeSubscription;
            });

            Optional<ChatUsage> optionalChatUsage = Optional.ofNullable(chatUsageService.findByUserIdAndChatVersion(user.getId(), user.getModel()));
            ChatUsage chatUsage = optionalChatUsage.orElseGet(() -> {
                ChatUsage newChatUsage = new ChatUsage();
                newChatUsage.setUser(user);
                newChatUsage.setChatVersion(user.getModel());
                newChatUsage.setLastActivity(LocalDateTime.now());
                newChatUsage.setSubscription(subscription);
                newChatUsage.setRequestsMade(0);
                newChatUsage.setTokensReceived(0);
                newChatUsage.setTokensSent(0);
                newChatUsage.setSubscription(subscription);
                chatUsageService.save(newChatUsage);
                log.info("New chat usage record created for user: {}", username);
                return newChatUsage;
            });

            String response;
            if (userMessage.startsWith("/")) {
                if (user.isAdmin()) {
                    response = commandHandler.handleAdminCommand(telegramId, username, userMessage);
                } else {
                    response = commandHandler.handleUserCommand(telegramId, username, userMessage);
                }
                } else {
                //esponse = processUserMessage(telegramId, userMessage);
                response = commandHandler.sendMessage(telegramId, userMessage);
            }

            sendMessage(Long.valueOf(chatId), response);
        }
    }

    private boolean isAdminCommand(String command, Long telegramId) {
        return command.equals("/stats") && userService.findByTelegramId(telegramId)
                .map(User::isAdmin)
                .orElse(false);
    }

    private String processUserMessage(Long telegramId, String userMessage) {
        // Логика обработки обычных сообщений пользователя




        return "Received your message: " + userMessage;
    }

    public void sendMessage(Long chatId, String message) {
        if (message.length() > 4096) {
            sendLongMessage(chatId, message);
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId.toString());
            sendMessage.setText(message);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    public void sendLongMessage(Long chatId, String message) {
        List<String> parts = MessageUtils.splitMessage(message, 4096);
        for (String part : parts) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId.toString());
            sendMessage.setText(part);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

}
