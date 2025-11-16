package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class LogUpdateRequest {
    private String todaySummary;
    private String tomorrowPlan;
    private String helpNeeded;
    private List<Long> taskId; // ["1","3"] -> [1,3]
}
