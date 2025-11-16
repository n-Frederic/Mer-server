package com.example.demo.service;

import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DeepseekService implements AiService{
    private static final String API_KEY = "sk-af3782ba7b664de5b908ba4fd2dbfaf8";
    private static final String URL = "https://api.deepseek.com/chat/completions";

    private final OkHttpClient client = new OkHttpClient();

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

        Request request = new Request.Builder()
                .url(URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(RequestBody.create(
                        payload,
                        MediaType.parse("application/json")
                ))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "输出好像出现了问题...";
    }
}
