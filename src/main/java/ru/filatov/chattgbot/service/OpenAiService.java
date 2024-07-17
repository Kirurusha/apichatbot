package ru.filatov.chattgbot.service;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.filatov.chattgbot.entity.ChatContext;
import ru.filatov.chattgbot.entity.Message;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public String getResponseFromGPT(ChatContext chatContext, String model) {
        // Преобразуем список сообщений в JSON массив
        JSONArray messageArray = new JSONArray();
        for (Message message : chatContext.getMessages()) {
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("role", message.getRole());
            jsonMessage.put("content", message.getContent());
            messageArray.put(jsonMessage);
        }

        HttpResponse<JsonNode> response = Unirest.post("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + openAiApiKey)
                .header("Content-Type", "application/json")
                .body(new JSONObject()
                        .put("model", model)
                        .put("messages", messageArray)
                        .put("max_tokens", 1500)
                        .put("temperature", 0.7)
                        .put("top_p", 1.0)
                        .put("frequency_penalty", 0)
                        .put("presence_penalty", 0))
                .asJson();

        if (response.getStatus() == 200) {
            JSONObject responseBody = response.getBody().getObject();
            JSONArray choices = responseBody.getJSONArray("choices");
            return choices.getJSONObject(0).getJSONObject("message").getString("content").trim();
        } else {
            // Логирование ошибки
            System.err.println("Ошибка при обращении к GPT API: " + response.getStatus() + " " + response.getBody().toString());
            return "Ошибка при обращении к GPT API";
        }
    }

    // Метод для вычисления количества использованных токенов
    public int calculateTokensUsed(String userMessage, String response) {
        int userMessageTokens = countTokens(userMessage);
        int responseTokens = countTokens(response);
        return userMessageTokens + responseTokens;
    }

    // Примерная реализация метода подсчета токенов (может быть изменена в зависимости от конкретных правил)
    private int countTokens(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        // Разделение на слова и знаки пунктуации
        String[] tokens = content.split("\\s+|(?=\\p{Punct})|(?<=\\p{Punct})");
        return tokens.length;
    }
}
