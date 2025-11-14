package com.example.demo.repository;

import com.example.demo.entity.TaskReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskReportRepository extends JpaRepository<TaskReport, Long> {
    List<TaskReport> findByTaskId(Long taskId);

}
