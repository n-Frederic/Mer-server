package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.dto.WeeklySummaryResponse;
import com.example.demo.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<?> getWeeklySummary() {
        try {
            Long userId = UserContext.getCurrentUserId();

            WeeklySummaryResponse response = analyticsService.generateWeeklySummary(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "ok", false,
                    "error", "AI summary generation failed",
                    "message", e.getMessage()
            ));
        }
    }
}
