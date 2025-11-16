package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ai_analysis")
public class AiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @Column(nullable = false)
    private String title;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();

    @Column(name = "generated_by")
    private Long generatedBy; // user ID

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "metrics_json", columnDefinition = "JSON")
    private String metricsJson; // JSON string

    @Column(columnDefinition = "TEXT")
    private String suggestions;
}
