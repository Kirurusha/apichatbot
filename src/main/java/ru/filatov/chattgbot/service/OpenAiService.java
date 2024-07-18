package ru.filatov.chattgbot.service;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.filatov.chattgbot.entity.ChatContext;
import ru.filatov.chattgbot.entity.Message;

import java.util.stream.Collectors;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public String getResponseFromGPT(ChatContext chatContext, String model) {
        // Преобразуем список сообщений в JSON массив
        JSONArray messageArray = new JSONArray(chatContext.getMessages().stream()
                .map(message -> new JSONObject()
                        .put("role", message.getRole())
                        .put("content", message.getContent()))
                .collect(Collectors.toList()));

        // Логирование сформированного массива сообщений
        System.out.println("Отправляемый контекст сообщений: " + messageArray.toString());

        // Формирование тела запроса
        JSONObject requestBody = new JSONObject()
                .put("model", model)
                .put("messages", messageArray)
                .put("max_tokens", 1500)
                .put("temperature", 0.7)
                .put("top_p", 1.0)
                .put("frequency_penalty", 0)
                .put("presence_penalty", 0);

        // Логирование тела запроса
        System.out.println("Request Body: " + requestBody.toString());

        HttpResponse<JsonNode> response;
        try {
            response = Unirest.post("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + openAiApiKey) // Используем trim() для удаления лишних пробелов
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .asJson();
        } catch (UnirestException e) {
            // Логирование ошибки HTTP запроса
            e.printStackTrace();
            return "Ошибка при выполнении HTTP запроса к GPT API";
        }

        if (response.getStatus() == 200) {
            JSONObject responseBody = response.getBody().getObject();
            JSONArray choices = responseBody.getJSONArray("choices");
            return choices.getJSONObject(0).getJSONObject("message").getString("content").trim();
        } else {
            // Логирование ошибки ответа от GPT API
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
