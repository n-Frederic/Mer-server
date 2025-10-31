package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_assignment") // 映射数据库表名
public class TaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增主键
    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Column(name = "task_id", nullable = false)
    private Long taskId; // 关联的任务ID

    @Column(name = "assignee_id", nullable = false)
    private Long assigneeId; // 被分配人ID（接收任务的用户）

    @Column(name = "assigned_by", nullable = false)
    private Long assignedBy; // 分配人ID（发起分配的用户）

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt; // 分配时间（默认当前时间，不允许更新）

    @Column(name = "accept_at")
    private LocalDateTime acceptAt; // 接收时间（可为空，任务被接收后设置）

    @Column(name = "finish_at")
    private LocalDateTime finishAt; // 完成时间（可为空，任务完成后设置）

    @Column(name = "progress_pct", nullable = false)
    private Integer progressPct = 0; // 进度百分比（默认0）

    // 构造方法
    public TaskAssignment() {}

    public TaskAssignment(Long taskId, Long assigneeId, Long assignedBy, LocalDateTime assignedAt) {

        this.taskId = taskId;
        this.assigneeId = assigneeId;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;

    }

    // 初始化必要字段的构造方法（可选）
    public TaskAssignment(Long taskId, Long assigneeId, Long assignedBy) {
        this.taskId = taskId;
        this.assigneeId = assigneeId;
        this.assignedBy = assignedBy;
        this.assignedAt = LocalDateTime.now(); // 默认为当前时间
        this.progressPct = 0;
    }

    // Getter 和 Setter
    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Long assignedBy) {
        this.assignedBy = assignedBy;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getAcceptAt() {
        return acceptAt;
    }

    public void setAcceptAt(LocalDateTime acceptAt) {
        this.acceptAt = acceptAt;
    }

    public LocalDateTime getFinishAt() {
        return finishAt;
    }

    public void setFinishAt(LocalDateTime finishAt) {
        this.finishAt = finishAt;
    }

    public Integer getProgressPct() {
        return progressPct;
    }

    public void setProgressPct(Integer progressPct) {
        this.progressPct = progressPct;
    }
}