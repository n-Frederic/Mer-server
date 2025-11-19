package com.example.demo.controller;

import com.example.demo.dto.CompanyTaskBatchDTO;
import com.example.demo.entity.CompanyTask;
import com.example.demo.service.CompanyTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/company-tasks")
public class CompanyTaskController {

    private final CompanyTaskService companyTaskService;

    public CompanyTaskController(CompanyTaskService companyTaskService) {
        this.companyTaskService = companyTaskService;
    }

    /**
     * GET /api/company-tasks
     * 获取所有公司任务详细信息
     */
    @GetMapping
    public Map<String, Object> getAllCompanyTasks() {
        List<CompanyTask> tasks = companyTaskService.getAllCompanyTasks();

        Map<String, Object> response = new HashMap<>();
        response.put("tasks", tasks);
        response.put("total", tasks.size());

        return response;
    }

    /**
     * GET /api/company-tasks/important
     * 获取公司重要任务列表（只返回标题）
     */
    @GetMapping("/important")
    public Map<String, Object> getCompanyImportantTasks() {
        List<String> tasks = companyTaskService.getCompanyImportantTasks();

        Map<String, Object> response = new HashMap<>();
        response.put("tasks", tasks);
        return response;
    }



    /**
     * GET /api/company-tasks/{id}
     * 根据ID获取任务详情
     */
    @GetMapping("/{id}")
    public CompanyTask getCompanyTaskById(@PathVariable Long id) {
        return companyTaskService.getCompanyTaskById(id);
    }

    /**
     * POST /api/company-tasks
     * 创建新的公司任务
     */
    @PostMapping
    public CompanyTask createCompanyTask(@RequestBody CompanyTask companyTask) {
        return companyTaskService.saveCompanyTask(companyTask);
    }

    /**
     * PUT /api/company-tasks/{id}
     * 更新公司任务
     */
    @PutMapping
    public List<CompanyTask> updateCompanyTask(@RequestBody CompanyTaskBatchDTO dto) {
        return companyTaskService.replaceAllCompanyTasks(dto.getTasks());
    }

    /**
     * DELETE /api/company-tasks/{id}
     * 删除公司任务
     */
    @DeleteMapping("/{id}")
    public void deleteCompanyTask(@PathVariable Long id) {
        companyTaskService.deleteCompanyTask(id);
    }
}
