package ru.filatov.chattgbot.util;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

public class BotUtils {
    public static String determineChatVersion(String userMessage) {
        // Логика определения версии чата по сообщению пользователя
        if (userMessage.contains("/setmodel_gpt3")) {
            return "gpt-3.5-turbo";
        } else if (userMessage.contains("/setmodel_gpt4")) {
            return "gpt-4";
        }
        return "default";
    }

    public static void sendMessage(TelegramLongPollingBot bot, Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
