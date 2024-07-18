package ru.filatov.chattgbot.utils;

import java.util.ArrayList;
import java.util.List;

public class MessageUtils {

    public static List<String> splitMessage(String message, int maxLength) {
        List<String> parts = new ArrayList<>();
        int length = message.length();
        for (int i = 0; i < length; i += maxLength) {
            parts.add(message.substring(i, Math.min(length, i + maxLength)));
        }
        return parts;
    }
}
