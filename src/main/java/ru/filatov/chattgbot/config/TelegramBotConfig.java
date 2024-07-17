package ru.filatov.chattgbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.filatov.chattgbot.bot.ChatGPTBot;
import ru.filatov.chattgbot.service.*;

@Configuration
public class TelegramBotConfig {

    private final BotConfig botConfig;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final ChatContextService chatContextService;
    private final ChatUsageService chatUsageService;
    private final CommandHandler commandHandler;

    public TelegramBotConfig(
            BotConfig botConfig,
            UserService userService,
            SubscriptionService subscriptionService,
            ChatContextService chatContextService,
            ChatUsageService chatUsageService,
            CommandHandler commandHandler
    ) {
        this.botConfig = botConfig;
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.chatContextService = chatContextService;
        this.chatUsageService = chatUsageService;
        this.commandHandler = commandHandler;
    }

    @Bean
    public ChatGPTBot chatGPTBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            ChatGPTBot bot = new ChatGPTBot(
                    botConfig.getTelegramBotToken(),
                    botConfig.getTelegramBotUsername(),
                    botConfig.getOpenAiApiKey(),
                    userService,
                    subscriptionService,
                    chatContextService,
                    chatUsageService,
                    commandHandler
            );
            botsApi.registerBot(bot);
            return bot;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }
}
