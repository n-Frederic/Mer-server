package com.example.demo.repository;

import com.example.demo.entity.Log_Task;
import com.example.demo.entity.LogTaskId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogTaskRepository extends JpaRepository<Log_Task, LogTaskId> {
    List<Log_Task> findById_LogId(Long logId);
}
