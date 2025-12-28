package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "task_report")
public class TaskReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "reporter_id")
    private Long reporterId;

    private String content;

    private String address;

    @Column(name = "attachments", length = 2000)
    private String attachments;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "status")
    private String status;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_by")
    private Long rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    // 辅助方法：获取附件URL列表
    @Transient
    public List<String> getAttachmentList() {
        if (attachments == null || attachments.isEmpty() || "[]".equals(attachments)) {
            return new ArrayList<>();
        }

        // 尝试解析JSON格式
        if (attachments.startsWith("[")) {
            try {
                // 简单解析JSON数组（不含复杂嵌套）
                String clean = attachments.replaceAll("[\\[\\]\"]", "");
                if (clean.isEmpty()) {
                    return new ArrayList<>();
                }
                return Arrays.asList(clean.split(","));
            } catch (Exception e) {
                // 如果JSON解析失败，尝试按竖线分隔
            }
        }

        // 按竖线分隔
        return Arrays.asList(attachments.split("\\|"));
    }

    // 辅助方法：设置附件URL列表
    @Transient
    public void setAttachmentList(List<String> attachmentUrls) {
        if (attachmentUrls == null || attachmentUrls.isEmpty()) {
            this.attachments = "[]";
        } else {
            // 使用竖线分隔存储
            this.attachments = String.join("|", attachmentUrls);
        }
    }

    // 辅助方法：添加单个附件URL
    @Transient
    public void addAttachment(String attachmentUrl) {
        List<String> currentList = getAttachmentList();
        currentList.add(attachmentUrl);
        setAttachmentList(currentList);
    }
}