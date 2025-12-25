package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class FeiShuBotService {

    @Value("${feishu.bot.webhook}")
    private String webhook;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送文本消息
     */
    public void sendText(String text) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("msg_type", "text");

            Map<String, Object> content = new HashMap<>();
            content.put("text", text);
            body.put("content", content);

            String json = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhook))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Feishu response: " + response.body());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}