package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.dto.LogRequestDTO;
import com.example.demo.dto.LogResponseDTO;
import com.example.demo.entity.Log;
import com.example.demo.entity.User;
import com.example.demo.service.LogService;
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
            @RequestParam(defaultValue = "9") int pageSize,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {

        Long authorId = UserContext.getCurrentUserId();

        Page<Log> logPage = logService.getLogsByUser(authorId,page, pageSize);

        List<Map<String, Object>> list = logPage.getContent().stream().map(log -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", "J-" + String.format("%03d", log.getId()));
            m.put("title", log.getTitle());
            m.put("date", log.getDate());
            m.put("summary", log.getSummary());
            m.put("content", log.getContent());
            m.put("authorId", "U-" + log.getAuthor().getId());
            m.put("taskId",log.getTaskId());
            m.put("authorName", log.getAuthor().getName());
            m.put("authorEmail", log.getAuthor().getEmail());
            m.put("createdAt", log.getCreatedAt());
            m.put("updatedAt", log.getUpdatedAt());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("list", list);
        response.put("total", logPage.getTotalElements());
        response.put("page", page);
        response.put("pageSize", pageSize);
        return response;
    }

    @PostMapping("/")
    public ResponseEntity<?> createJournal(
            @RequestHeader("Authorization") String authorization,
            @RequestBody LogRequestDTO request
    ) {
        try {
            // 解析 token（此处示例，你后续可接入 JWT 校验）
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Missing or invalid token");
            }

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

}
