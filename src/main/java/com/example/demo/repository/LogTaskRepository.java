package com.example.demo.repository;

import com.example.demo.entity.Log_Task;
import com.example.demo.entity.LogTaskId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogTaskRepository extends JpaRepository<Log_Task, LogTaskId> {
}
