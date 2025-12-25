package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class FeiShuBotService {

    @Value("${feishu.bot.webhook}")
    private String webhook;

    @Value("${feishu.bot.secret}")
    private String secret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送文本消息
     */
    public void sendText(String text) {
        try {
            long timestamp = Instant.now().getEpochSecond();
            String sign = genSign(timestamp, secret);

            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", String.valueOf(timestamp));
            body.put("sign", sign);

            Map<String, Object> content = new HashMap<>();
            content.put("text", text);

            Map<String, Object> msg = new HashMap<>();
            msg.put("msg_type", "text");
            msg.put("content", content);

            body.putAll(msg);

            String json = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhook))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成签名
     */
    private String genSign(long timestamp, String secret) throws Exception {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }
}

