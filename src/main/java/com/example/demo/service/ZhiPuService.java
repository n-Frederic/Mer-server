package com.example.demo.service;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class ZhiPuService implements AiService {

    private static final String API_KEY = "52c39572fb0f4d448dabcee5470cf82b.DjEfa1TILGr18SgQ";
    private static final String URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private static final Logger logger = LoggerFactory.getLogger(ZhiPuService.class);

    @Override
    public String summarize(String text) {
        String prompt = """
        请对以下日志生成本周总结，要求：
        1. 描述主要工作内容
        2. 概括关键成果与进展
        3. 点出遇到的主要问题
        4. 用自然语言输出，并避免重复

        日志内容：
        %s
        """.formatted(text);

        String payload = """
        {
            "model": "glm-4-flash",
            "messages": [
                {"role": "system", "content": "你是一个专业的工作日志分析助手"},
                {"role": "user", "content": "%s"}
            ]
        }
        """.formatted(escapeForJson(prompt));

        return askRawPayload(payload);
    }

    public String ask(String prompt) {
        String payload = """
        {
            "model": "glm-4-flash",
            "messages": [
                {"role": "system", "content": "你是一个专业且可靠的 AI 助手"},
                {"role": "user", "content": "%s"}
            ]
        }
        """.formatted(escapeForJson(prompt));

        return askRawPayload(payload);
    }

    public String askRawPayload(String jsonPayload) {
        RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                logger.warn("Zhipu returned empty body (status={})", response.code());
                return errorResponse("AI 服务无响应");
            }

            String respText = response.body().string();

            if (!response.isSuccessful()) {
                logger.error("Zhipu API error: status={}, body={}", response.code(), respText);
                return convertErrorResponse(response.code(), respText);
            }

            return respText;
        } catch (IOException e) {
            logger.error("IOException when calling Zhipu: {}", e.getMessage(), e);
            return errorResponse("AI 服务请求失败，请稍后再试");
        }
    }

    private String convertErrorResponse(int code, String rawBody) {
        if (code == 401) return errorResponse("智谱 API Key 无效");
        if (code == 429) return errorResponse("智谱 API 请求频率已超限");
        if (code == 500) return errorResponse("智谱服务内部错误");

        return errorResponse("AI 服务错误：" + rawBody);
    }

    private String errorResponse(String message) {
        return """
        {
          "ok": false,
          "error": "AI analysis failed",
          "message": "%s"
        }
        """.formatted(message);
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
