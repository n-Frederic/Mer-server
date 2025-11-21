package com.example.demo.controller;

import com.example.demo.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("tasks/{taskId}/reports")
public class TaskReportController {

    private final TaskService taskService;

    public TaskReportController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/{reporterId}/approve")
    public Map<String, Object> approve(
            @PathVariable Long taskId,
            @PathVariable Long reporterId,
            @RequestBody Map<String, String> body
    ) {
        Long approvedBy = Long.valueOf(body.get("approved_by"));
        LocalDateTime approvedAt = LocalDateTime.parse(body.get("approved_at"));

        taskService.approveReport(taskId, reporterId, approvedBy, approvedAt);

        return Map.of("ok", true, "message", "报告审批成功");
    }

    @PostMapping("/{reporterId}/reject")
    public Map<String, Object> reject(
            @PathVariable Long taskId,
            @PathVariable Long reporterId,
            @RequestBody Map<String, String> body
    ) {
        Long rejectedBy = Long.valueOf(body.get("rejected_by"));
        LocalDateTime rejectedAt = LocalDateTime.parse(body.get("rejected_at"));
        String reason = body.get("reason");

        taskService.rejectReport(taskId, reporterId, rejectedBy, rejectedAt, reason);

        return Map.of("ok", true, "message", "报告已拒绝");
    }

    @GetMapping("/check-all-approved")
    public Map<String, Object> checkAllApproved(@PathVariable Long taskId) {
        return taskService.checkAllApproved(taskId);
    }

    @PostMapping("/batch-approve")
    public Map<String, Object> batchApprove(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> body
    ) {
        Long approvedBy = Long.valueOf(body.get("approved_by"));
        LocalDateTime approvedAt = LocalDateTime.parse(body.get("approved_at"));

        return taskService.batchApprove(taskId, approvedBy, approvedAt);
    }

    @GetMapping("/statistics")
    public Map<String, Object> statistics(@PathVariable Long taskId) {
        return taskService.reportStatistics(taskId);
    }

    @GetMapping
    public Map<String, Object> getReports(@PathVariable Long taskId) {
        return taskService.getReports(taskId);
    }
}
