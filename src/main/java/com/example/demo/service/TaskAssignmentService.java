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
}
