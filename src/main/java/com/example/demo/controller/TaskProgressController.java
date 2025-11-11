package com.example.demo.controller;

import com.example.demo.dto.TaskProgressUpdateDTO;
import com.example.demo.service.TaskProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskProgressController {

    private final TaskProgressService taskProgressService;

    public TaskProgressController(TaskProgressService taskProgressService) {
        this.taskProgressService = taskProgressService;
    }

    @PutMapping("/{taskId}/progress")
    public ResponseEntity<Map<String, Object>> updateProgress(
            @PathVariable Long taskId,
            @RequestBody TaskProgressUpdateDTO request
    ) {

        try {
            taskProgressService.updateTaskProgress(taskId, request);

            return ResponseEntity.ok(
                    Map.of(
                            "ok", true,
                            "message", "进度更新成功"
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "ok", false,
                            "message", e.getMessage()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "ok", false,
                            "message", "服务器内部错误"
                    )
            );
        }
    }
}
