package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_report")
public class TaskReport {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Setter
    @Getter
    @Column(name = "task_id")
    private Long taskId;

    @Setter
    @Getter
    @Column(name = "reporter_id")
    private Long reporterId;

    @Setter
    @Getter
    private String content;

    @Setter
    @Getter
    private String address;

    @Getter
    @Setter
    private String attachments; // JSON 字符串

    @Setter
    @Getter
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
