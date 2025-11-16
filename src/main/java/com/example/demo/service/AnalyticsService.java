package com.example.demo.service;

import com.example.demo.component.KeywordExtractor;
import com.example.demo.dto.Keyword;
import com.example.demo.dto.StatusCount;
import com.example.demo.dto.WeeklySummaryResponse;
import com.example.demo.entity.AiAnalysis;
import com.example.demo.entity.Log;
import com.example.demo.repository.AiAnalysisRepository;
import com.example.demo.repository.LogRepository;
import com.example.demo.repository.TaskAssignmentRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final LogRepository logRepository;
    private final DeepseekService deepseekService;
    private final KeywordExtractor keywordExtractor;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;

    public AnalyticsService(LogRepository logRepository, DeepseekService deepseekService, KeywordExtractor keywordExtractor, AiAnalysisRepository aiAnalysisRepository, TaskAssignmentRepository taskAssignmentRepository) {
        this.logRepository = logRepository;
        this.deepseekService = deepseekService;
        this.keywordExtractor = keywordExtractor;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
    }

    public WeeklySummaryResponse generateWeeklySummary(Long userId) {

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

        String summary = deepseekService.summarize(text);
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
                summary,
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
}
