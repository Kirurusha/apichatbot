package ru.filatov.chattgbot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.filatov.chattgbot.entity.ChatContext;
import ru.filatov.chattgbot.entity.ChatUsage;
import ru.filatov.chattgbot.entity.User;

import java.util.Optional;

@Service
public class CommandHandler {

    private final UserService userService;
    private final ChatContextService chatContextService;
    private final ChatUsageService chatUsageService;
    private final SubscriptionService subscriptionService;
    private final AdminService adminService;
    private final OpenAiService openAiService;

    public CommandHandler(UserService userService, ChatContextService chatContextService, ChatUsageService chatUsageService, SubscriptionService subscriptionService, AdminService adminService, OpenAiService openAiService) {
        this.userService = userService;
        this.chatContextService = chatContextService;
        this.chatUsageService = chatUsageService;
        this.subscriptionService = subscriptionService;
        this.adminService = adminService;
        this.openAiService = openAiService;
    }

    public String handleUserCommand(Long telegramId, String username, String command) {
        switch (command) {
            case "/start":
                return handleStartCommand(telegramId, username);
            case "/help":
                return handleHelpCommand();
            case "/setmodel":
                return handleSetModelCommand(telegramId, command);
            case "/setmodel_gpt3.5-turbo-0125":
                userService.setUserModel(telegramId, "gpt-3.5-turbo-0125");
                return "Model has been set to gpt-3.5-turbo-0125";
            case "/setmodel_gpt4":
                userService.setUserModel(telegramId, "gpt-4o");
                return "Model has been set to gpt-4";
            case "/clearcontext":
                return handleClearContextCommand(telegramId);
            case "/stats":
                return handleUserStatsCommand(telegramId);
            case "/allstats":
                return handleAllUsersStatsCommand();
            default:
                return "Unknown command";
        }
    }

    private String handleClearContextCommand(Long telegramId) {
        Optional<User> userOpt = userService.findByTelegramId(telegramId);
        if (userOpt.isEmpty()) {
            return "User not found.";
        }

        User user = userOpt.get();
        chatContextService.clearChatContext(user.getId());

        return "Chat context has been cleared.";
    }


    @Transactional
    public String handleAdminCommand(Long telegramId, String username, String command) {
        Optional<User> userOpt = userService.findByTelegramId(telegramId);
        if (userOpt.isPresent() && !userOpt.get().isAdmin()) {
            return "You do not have permission to execute this command.";
        }

        switch (command) {
            case "/stats":
                return handleUserStatsCommand(telegramId);
                case "/allstats":
                return handleAllUsersStatsCommand();

            default:
                return "Unknown command";
        }
    }

    private String handleStartCommand(Long telegramId, String username) {
        Optional<User> userOpt = userService.findByTelegramId(telegramId);
        User user;
        if (userOpt.isEmpty()) {
            user = new User();
            user.setTelegramId(telegramId);
            user.setUsername(username);
            user.setActive(true);
            user.setAdmin(false);
            user.setRole("USER");
            user.setModel("gpt-3.5"); // Устанавливаем модель по умолчанию
            userService.save(user);

            subscriptionService.createFreeSubscriptionForUser(user);

            ChatContext chatContext = new ChatContext();
            chatContext.setUser(user);
            chatContext.setCurrentTokenCount(0);
            chatContextService.save(chatContext);

            ChatUsage chatUsage = new ChatUsage();
            chatUsage.setUser(user);
            chatUsage.setChatVersion("default");
            chatUsage.setRequestsMade(0);
            chatUsage.setTokensSent(0);
            chatUsage.setTokensReceived(0);
            chatUsageService.save(chatUsage);
        } else {
            user = userOpt.get();
        }

        return "Welcome, " + username + "!";
    }

    private String handleHelpCommand() {
        return "Here are the available commands:\n/start - Start using the bot\n/help - Get help\n/setmodel <model> - Set the GPT model (e.g., gpt-4.0)";
    }

    private String handleSetModelCommand(Long telegramId, String command) {
        String[] parts = command.split(" ");
        if (parts.length < 2) {
            return "Please specify the model. Example: /setmodel gpt-4.0";
        }

        String model = parts[1];
        userService.setUserModel(telegramId, model);
        return "Model has been set to " + model;
    }

    private String handleStatsCommand() {
        return "Statistics: ...";
    }
    private String handleUserStatsCommand(Long telegramId) {
        Optional<User> userOpt = userService.findByTelegramId(telegramId);
        if (userOpt.isEmpty()) {
            return "User not found.";
        }
        User user = userOpt.get();
        return chatUsageService.getUserStatistics(user.getId());
    }

    private String handleAllUsersStatsCommand() {
        return chatUsageService.getFullStatistics();
    }

    @Transactional
    public String sendMessage(Long telegramId, String userMessage) {
        Optional<User> userOpt = userService.findByTelegramId(telegramId);
        if (userOpt.isEmpty()) {
            return "User not found.";
        }
        User user = userOpt.get();
        ChatContext chatContext = chatContextService.findByUserId(user.getId());
        if (chatContext == null) {
            return "Chat context not found.";
        }

        String chatVersion = user.getModel(); // Используем модель как версию чата

        ChatUsage chatUsage = chatUsageService.findByUserIdAndVersion(user.getId(), chatVersion);
        if (chatUsage == null) {
            chatUsage = new ChatUsage();
            chatUsage.setUser(user);
            chatUsage.setChatVersion(chatVersion);
            chatUsage.setRequestsMade(0);
            chatUsage.setTokensSent(0);
            chatUsage.setTokensReceived(0);
            chatUsageService.save(chatUsage);
        }

        if (subscriptionService.isRequestLimitReached(user.getId(), chatVersion)) {
            System.out.println("Request limit reached for this subscription period.");
            return "Request limit reached for this subscription period.";
        }

        chatContext.addMessage(userMessage, "user");

        String response = openAiService.getResponseFromGPT(chatContext, user.getModel());

        chatContext.addMessage(response, "assistant");

        int tokensUsed = openAiService.calculateTokensUsed(userMessage, response);
        chatUsage.incrementTokensSent(tokensUsed);
        chatUsage.incrementTokensReceived(tokensUsed);
        chatUsage.incrementRequestsMade();

        chatContextService.save(chatContext);
        chatUsageService.save(chatUsage);

        return response;
    }
}
