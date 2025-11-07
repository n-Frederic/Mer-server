package com.example.demo.service;

import com.example.demo.entity.Department;
import com.example.demo.entity.Login;
import com.example.demo.repository.CompanyTaskRepository;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }
    public List<Department> getDepartment() {
        return departmentRepository.findAll();
    }

}
