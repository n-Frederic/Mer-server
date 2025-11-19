package com.example.demo.service;

import com.example.demo.component.KeywordExtractor;
import com.example.demo.dto.*;
import com.example.demo.entity.AiAnalysis;
import com.example.demo.entity.Log;
import com.example.demo.repository.AiAnalysisRepository;
import com.example.demo.repository.LogRepository;
import com.example.demo.repository.TaskAssignmentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AnalyticsService {

    private final LogRepository logRepository;
    private final DeepSeekService deepseekService;
    private final ZhiPuService zhiPuService;
    private final KeywordExtractor keywordExtractor;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;

    public AnalyticsService(LogRepository logRepository, DeepSeekService deepseekService, ZhiPuService zhiPuService, KeywordExtractor keywordExtractor, AiAnalysisRepository aiAnalysisRepository, TaskAssignmentRepository taskAssignmentRepository) {
        this.logRepository = logRepository;
        this.deepseekService = deepseekService;
        this.zhiPuService = zhiPuService;
        this.keywordExtractor = keywordExtractor;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
    }

    public WeeklySummaryResponse generateWeeklySummary(Long userId) throws JsonProcessingException {

        LocalDateTime startOfWeek = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .atStartOfDay();

        List<Log> logs = logRepository.findLogsForCurrentWeek(userId, startOfWeek);

        if (logs.isEmpty()) {
            return new WeeklySummaryResponse(
                    false,
                    "本周暂无日志，无法生成总结",
                    List.of(),
                    LocalDateTime.now()
            );
        }

        String text = logs.stream()
                .map(l -> "Summary:" + l.getSummary() +
                        "\nTomorrow:" + l.getTomorrowPlan() +
                        "\nHelp:" + l.getHelpNeeded())
                .collect(Collectors.joining("\n\n"));

        String summary = zhiPuService.summarize(text);
        summary = extractContentFromZhiPu(summary);
        List<Keyword> keywords = keywordExtractor.extractTopKeywords(text);

        AiAnalysis record = new AiAnalysis();
        record.setTitle("Weekly Summary");
        record.setGeneratedBy(userId);
        record.setSummary(summary);
        record.setMetricsJson(null);
        record.setSuggestions(null);

        aiAnalysisRepository.save(record);

        return new WeeklySummaryResponse(
                true,
                summary,            // 直接使用自然语言
                keywords,
                LocalDateTime.now()
        );
    }

    public List<StatusCount> getWeeklyChartData(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        return taskAssignmentRepository.findWeeklyTaskStatusCount(userId, start, end);
    }

    public FortuneResponse analyzeFortune(Long userId) throws JsonProcessingException {

        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);

        List<Log> logs = logRepository.findLogsForUserSince(userId, monday);

        String helpNeeded = logs.stream()
                .map(Log::getHelpNeeded)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        String prompt = """
                你是一位温柔、有趣、但专业的运势顾问。
                请基于以下内容，生成本周运势分析与今日幸运建议：

                参考数据：
                --- helpNeeded ---
                %s

                要求输出 JSON:
                {
                  "analysis": "整体运势描述",
                  "suggestion": {
                    "color": "某种颜色",
                    "time": "某个时间段",
                    "direction": "某个方向",
                    "number": 数字
                  }
                }

                """.formatted(helpNeeded);

        String aiResponse = zhiPuService.ask(prompt);
        String content = extractContentFromZhiPu(aiResponse);
        content = cleanContent(content);

        if (!isValidJson(content)) {
            throw new RuntimeException("Invalid JSON from AI: " + content);
        }
        return parseFP(content);
    }

    public PersonalityResponse analyzePersonality(Long userId) throws JsonProcessingException {
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);

        List<Log> logs = logRepository.findLogsForUserSince(userId, threeMonthsAgo);

        if (logs.isEmpty()) {
            throw new RuntimeException("No log data available for personality analysis");
        }

        String corpus = logs.stream()
                .flatMap(l -> Stream.of(l.getSummary(), l.getTomorrowPlan(), l.getHelpNeeded()))
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        String prompt = """
                你是一个心理学和行为分析专家。请根据以下用户日志内容，推断其 MBTI 类型。

                日志内容：
                %s

                请根据 MBTI 四大维度分析：
                1. 精力来源：内向 I / 外向 E
                2. 信息获取：实感 S / 直觉 N
                3. 决策方式：思考 T / 情感 F
                4. 生活态度：判断 J / 知觉 P

                输出 JSON 格式：
                {
                  "type": "ISTJ",
                  "analysis_summary": "...",
                  "breakdown": [
                     {"dimension": "...", "type": "...", "description": "..."}
                  ]
                }

                回答内容必须是合法 JSON。
                """.formatted(corpus);

        String aiResponse = zhiPuService.ask(prompt);
        String content = extractContentFromZhiPu(aiResponse);
        content = cleanContent(content);

        if (!isValidJson(content)) {
            throw new RuntimeException("Invalid JSON from AI: " + content);
        }
        return parsePP(content);
    }

    private PersonalityResponse parsePP(String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, PersonalityResponse.class);
    }

    private FortuneResponse parseFP(String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, FortuneResponse.class);
    }

    private WeeklySummaryResponse parseWSR(String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, WeeklySummaryResponse.class);
    }

    private String extractContentFromZhiPu(String responseJson) {
        try {
            JsonNode root = new ObjectMapper().readTree(responseJson);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }

    private boolean isValidJson(String content) {
        try {
            new ObjectMapper().readTree(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String cleanContent(String content) {
        content = content
                .replace("```json", "")
                .replace("```", "")
                .trim();
        return content;
    }


}
