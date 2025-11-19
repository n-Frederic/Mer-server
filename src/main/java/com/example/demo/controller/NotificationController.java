package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.entity.Notification;
import com.example.demo.service.LoginService;
import com.example.demo.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Map<String, Object> getNotification(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int pageSize) {

        Long userId = UserContext.getCurrentUserId();
        // 调用 service 获取分页数据（注意：page 参数在 PageRequest 中是从 0 开始的，这里需要减 1）
        Page<Notification> notificationPage = notificationService.getNotifications(page - 1, pageSize, userId);

        // 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);

        // 组装 data 部分
        Map<String, Object> data = new HashMap<>();
        // 转换为前端需要的 list 格式
        List<Map<String, Object>> notificationList = notificationPage.getContent().stream()
                .map(notification -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("notif_id", notification.getNotifId()); // 假设主键字段为 id
                    item.put("user_id", notification.getUserId());
                    item.put("type", notification.getType());
                    item.put("relevent_id", notification.getReleventId());
                    item.put("title", notification.getTitle());
                    item.put("body", notification.getBody());
                    item.put("is_read", notification.getRead()); // 假设字段为 isRead
                    item.put("created_at", notification.getCreatedAt() != null ?
                            notification.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-MM-dd HH:mm:ss")) : null);
                    return item;
                })
                .collect(Collectors.toList());

        data.put("list", notificationList);
        data.put("total", notificationPage.getTotalElements()); // 总条数
        data.put("page", page); // 前端传入的页码（从 1 开始）
        data.put("pageSize", pageSize); // 每页条数

        result.put("data", data);
        return result;
    }

}
