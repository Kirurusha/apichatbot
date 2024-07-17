package ru.filatov.chattgbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Value("${telegram.bot.token}")
    private String telegramBotToken;

    @Value("${telegram.bot.username}")
    private String telegramBotUsername;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public String getTelegramBotToken() {
        return telegramBotToken;
    }

    public String getTelegramBotUsername() {
        return telegramBotUsername;
    }

    public String getOpenAiApiKey() {
        return openAiApiKey;
    }
}
