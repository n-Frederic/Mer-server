package com.example.demo.service;

import com.example.demo.dto.TaskProgressUpdateDTO;
import com.example.demo.entity.TaskAssignment;
import com.example.demo.repository.TaskAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskAssignmentService {

    private final TaskAssignmentRepository taskAssignmentRepository;

    public TaskAssignmentService(TaskAssignmentRepository taskAssignmentRepository) {
        this.taskAssignmentRepository = taskAssignmentRepository;
    }

    public void updateTaskProgress(Long taskId, TaskProgressUpdateDTO request) {

        // userId 是 "U-1001" → 需要去掉前缀转换成 Long
        Long userId = Long.parseLong(request.getUserId().replace("U-", ""));

        Integer pct = request.getProgressPct();
        if (pct < 0 || pct > 100) {
            throw new IllegalArgumentException("进度百分比必须在 0-100 范围内");
        }

        TaskAssignment assignment = taskAssignmentRepository
                .findByTaskIdAndAssigneeId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("该任务未分配给此用户"));

        assignment.setProgressPct(pct);
        taskAssignmentRepository.save(assignment);
    }
}
