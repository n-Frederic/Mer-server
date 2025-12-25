package com.example.demo.component;

import com.example.demo.entity.Task;
import com.example.demo.repository.TaskRepository;
import com.example.demo.service.TaskDeadlineService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.time.Instant;
import java.util.List;

@Component
public class DeadlineInitializer {

    private final TaskRepository taskRepository;
    private final TaskDeadlineService taskDeadlineService;

    public DeadlineInitializer(TaskRepository taskRepository, TaskDeadlineService taskDeadlineService) {
        this.taskRepository = taskRepository;
        this.taskDeadlineService = taskDeadlineService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initDeadlines() {
        Instant now = Instant.now();
        List<Task> pendingTasks = taskRepository.findByDueAtAfter(now);
        for (Task task : pendingTasks) {
            taskDeadlineService.scheduleDeadlineNotification(
                    task.getTitle(),
                    task.getDueAt()
            );
        }
        System.out.println("Registered " + pendingTasks.size() + " pending deadline notifications.");
    }
}
