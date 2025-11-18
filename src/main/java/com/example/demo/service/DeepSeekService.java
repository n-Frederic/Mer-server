package com.example.demo.service;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DeepSeekService implements AiService{
    private static final String API_KEY = "sk-af3782ba7b664de5b908ba4fd2dbfaf8";
    private static final String URL = "https://api.deepseek.com/chat/completions";

    private final OkHttpClient client = new OkHttpClient();
    private static final Logger logger = LoggerFactory.getLogger(DeepSeekService.class);

    @Override
    public String summarize(String text){
        String payload = """
        {
            "model": "deepseek-chat",
            "messages": [
                {"role": "system", "content": "你是一个专业的文本总结助手,请根据这些日志内容详细分析他这周的工作情况，并生成总结"},
                {"role": "user", "content": "%s"}
            ]
        }
        """.formatted("请对以下日志生成本周总结，要求：\\n1. 描述主要工作内容\\n2. 概括关键成果与进展\\n3. 点出遇到的主要问题\\n4. 用自然语言输出，并避免重复\\n\\n日志内容：\\n" + text);

        askRawPayload(payload);

        return "输出好像出现了问题...";
    }

    public String ask(String prompt) {
        // 如果你传入的是已经格式化好的 JSON payload（如你的示例），则可以直接用 askRawPayload()
        // 本方法会把 prompt 放进一个通用的 messages 模板里（如果你直接传完整 payload，请用 askRawPayload）
        String payload = """
                {
                  "model": "deepseek-chat",
                  "messages": [
                    {"role":"system","content":"You are a helpful assistant."},
                    {"role":"user","content":"%s"}
                  ]
                }
                """.formatted(escapeForJson(prompt));

        return askRawPayload(payload);
    }

    public String askRawPayload(String jsonPayload) {
        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));

        Request.Builder reqBuilder = new Request.Builder()
                .url(URL)
                .post(body)
                .header("Content-Type", "application/json");

        if (!API_KEY.isBlank()) {
            reqBuilder.header("Authorization", "Bearer " + API_KEY);
        }

        Request request = reqBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                logger.warn("DeepSeek returned empty body (status={})", response.code());
                throw new RuntimeException("DeepSeek returned empty body");
            }

            String respText = response.body().string();
            if (!response.isSuccessful()) {
                logger.error("DeepSeek API error: status={}, body={}", response.code(), respText);

                if (response.code() == 402) {
                    throw new RuntimeException("DeepSeek API 余额不足，请充值。当前响应: " + respText);
                } else if (response.code() == 401) {
                    throw new RuntimeException("DeepSeek API 密钥无效");
                } else if (response.code() == 429) {
                    throw new RuntimeException("DeepSeek API 请求频率超限");
                } else {
                    throw new RuntimeException("DeepSeek API error: " + response.code() + " - " + respText);
                }
            }

            return respText;
        } catch (IOException e) {
            logger.error("IOException when calling DeepSeek: {}", e.getMessage(), e);
            throw new RuntimeException("AI service call failed", e);
        }

    }

    private String escapeForJson(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
