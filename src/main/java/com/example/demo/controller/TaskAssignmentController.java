package com.example.demo.controller;

import com.example.demo.dto.TaskProgressUpdateDTO;
import com.example.demo.service.TaskAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskAssignmentController {

    private final TaskAssignmentService taskAssignmentService;

    public TaskAssignmentController(TaskAssignmentService taskAssignmentService) {
        this.taskAssignmentService = taskAssignmentService;
    }
}
