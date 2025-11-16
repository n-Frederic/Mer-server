package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.dto.LogRequestDTO;
import com.example.demo.dto.LogResponseDTO;
import com.example.demo.dto.LogUpdateRequest;
import com.example.demo.entity.Log;
import com.example.demo.entity.Log_Task;
import com.example.demo.entity.User;
import com.example.demo.service.LogService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("journals")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/")
    public Map<String, Object> getUserLogs(
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int pageSize)
    {

        Long authorId = UserContext.getCurrentUserId();

        // 分页读取日志
        Page<Log> logPage = logService.getLogsByUser(authorId, page, pageSize);

        // list 内容处理
        List<Map<String, Object>> list = logPage.getContent().stream().map(log -> {

            List<Map<String, Object>> relatedTasks = logService.getTasksByLogId(log.getId());

            // 2. keywords（你以后可接入 NLP 做关键词分析）
            List<Map<String, Object>> keywords = new ArrayList<>();

            // 3. 主体 log 信息
            Map<String, Object> m = new HashMap<>();
            m.put("log_id", log.getId());
            m.put("log_date", log.getDate());
            m.put("todaySummary", log.getSummary());
            m.put("tomorrowPlan", log.getTomorrowPlan());
            m.put("helpNeeded", log.getHelpNeeded());
            m.put("status", log.getStatus());
            m.put("created_at", log.getCreatedAt());
            m.put("updated_at", log.getUpdatedAt());

            // 4. 作者信息
            Map<String, Object> author = new HashMap<>();
            author.put("user_id", log.getAuthor().getId());
            author.put("name", log.getAuthor().getName());
            author.put("email", log.getAuthor().getEmail());
            author.put("avatar_url", log.getAuthor().getAvatar_url()); // 如果没有字段可用 null
            m.put("author_info", author);

            // 5. 关联任务
            m.put("related_tasks", relatedTasks);

            // 6. 关键词
            m.put("keywords", keywords);

            return m;
        }).collect(Collectors.toList());

        // data 区域
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", logPage.getTotalElements());
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("hasNext", logPage.hasNext());

        // 最外层返回结构：与前端需求完全一致
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);

        return response;
    }

    @PostMapping("/")
    public ResponseEntity<?> createJournal(
            @RequestHeader("Authorization") String authorization,
            @RequestBody LogRequestDTO request
    ) {
        try {
            Long userId = UserContext.getCurrentUserId();
            User author = new User();
            author.setId(userId);

            LogResponseDTO response = logService.createLog(request, author);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 按你接口约定，失败时返回原请求体
            return ResponseEntity.badRequest().body(request);
        }
    }

    @GetMapping("/scoped")
    public Map<String, Object> getScopedLogs(
            @RequestParam String mode,
            @RequestParam(required = false) String memberIds,
            @RequestParam(defaultValue = "all") String timeFilter,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {

        var logPage = logService.getScopedLogs(mode, memberIds, timeFilter, keyword, page, pageSize);

        List<Map<String, Object>> list = logPage.getContent().stream().map(log -> {

            List<Long> taskIds = logService.getTaskIdsByLogId(log.getId());

            Map<String, Object> m = new HashMap<>();
            m.put("log_id", log.getId());
            m.put("user_id", log.getAuthor().getId());
            m.put("task_id", taskIds);
            m.put("log_date", log.getDate());
            m.put("created_at", log.getCreatedAt());
            m.put("updated_at", log.getUpdatedAt());

            m.put("todaySummary", log.getSummary());
            m.put("tomorrowPlan", log.getTomorrowPlan());
            m.put("helpNeeded", log.getHelpNeeded());
            m.put("status", log.getStatus());
            m.put("tags", logService.getTagsForLog(log.getId()));

            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", logPage.getTotalElements());
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("hasNext", logPage.hasNext());

        return data;
    }

    @DeleteMapping("/{journalId}")
    public ResponseEntity<Map<String, Object>> deleteJournal(
            @PathVariable Long journalId
    ) {
        try {
            logService.deleteJournal(journalId);

            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "日志删除成功"
            ));

        } catch (DataIntegrityViolationException e) {
            // 外键约束导致无法删除
            return ResponseEntity.status(400).body(Map.of(
                    "ok", false,
                    "error", "ConstraintViolation",
                    "message", "该日志无法删除"
            ));

        } catch (NoSuchElementException e) {
            // 日志不存在
            return ResponseEntity.status(404).body(Map.of(
                    "ok", false,
                    "message", "日志不存在"
            ));

        } catch (Exception e) {
            // 其他异常
            return ResponseEntity.status(500).body(Map.of(
                    "ok", false,
                    "message", "服务器错误"
            ));
        }
    }

    @GetMapping("/{logId}")
    public ResponseEntity<Map<String, Object>> getJournalDetail(@PathVariable Long logId) {
        return ResponseEntity.ok(logService.getJournalDetail(logId));
    }

    @PutMapping("/{logId}")
    public ResponseEntity<?> updateJournal(
            @PathVariable Long logId,
            @RequestBody LogUpdateRequest request
    ) {
        boolean ok = logService.updateLog(logId, request);

        if (!ok) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "ok", false,
                            "error", "Invalid data or journal"
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "message", "日志更新成功"
        ));
    }


}
