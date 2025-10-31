package com.example.demo.repository;

import com.example.demo.entity.Task;
import com.example.demo.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
}
