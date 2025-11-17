package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class WeeklySummaryResponse {
    private boolean ok;
    private String summary;
    private List<Keyword> keywords;
    private LocalDateTime generatedAt;
}
