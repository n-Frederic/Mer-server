package com.example.demo.controller;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.entity.Department;
import com.example.demo.service.DepartmentService;
import com.example.demo.service.LoginService;
import com.example.demo.utils.ResponseUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("department")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public Map<String, Object> department() {
        // 从Service获取部门列表（List<Department>）
        List<Department> deptList = departmentService.getDepartment();
        // 调用工具函数包装响应结构
        return ResponseUtils.wrapResponse(true, deptList);
    }

}
