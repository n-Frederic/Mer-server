package com.example.demo.service;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.ScheduledFuture;

@Service
public class TaskDeadlineService {

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final FeiShuBotService feiShuBotService;

    public TaskDeadlineService(FeiShuBotService feiShuBotService) {
        this.feiShuBotService = feiShuBotService;
    }

    @PostConstruct
    public void init() {
        scheduler.setPoolSize(10);
        scheduler.initialize();
    }

    public ScheduledFuture<?> scheduleDeadlineNotification(String taskTitle, Instant dueAt) {
        Instant realUtcDueAt = dueAt.minusSeconds(8 * 3600);

        Instant now = Instant.now();
        System.out.println("--- 最终时间对齐 ---");
        System.out.println("数据库原始 (误解析): " + dueAt);
        System.out.println("系统当前 UTC: " + now);
        System.out.println("修正后触发 UTC: " + realUtcDueAt);

        if (realUtcDueAt.isBefore(now)) {
            System.out.println("状态: 已过期，立即补发消息");
            feiShuBotService.sendText("⏰ 任务截止！任务名称：" + taskTitle);
            return null;
        }

        System.out.println("状态: 准时排队中...");
        return scheduler.schedule(
                () -> feiShuBotService.sendText("⏰ 任务截止！任务名称：" + taskTitle),
                realUtcDueAt
        );
    }


}
