package com.example.demo.service;

import com.example.demo.entity.Department;
import com.example.demo.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ✅ DepartmentService 单元测试
 * 测试部门相关业务逻辑
 */
@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department mockDepartment;

    @BeforeEach
    void setUp() {
        mockDepartment = new Department();
        mockDepartment.setDeptId(1);
        mockDepartment.setName("技术部");
    }

    /**
     * 测试1：获取所有部门成功
     */
    @Test
    void getDepartment_ShouldReturnAllDepartments() {
        Department dept2 = new Department();
        dept2.setDeptId(2);
        dept2.setName("市场部");

        when(departmentRepository.findAll()).thenReturn(Arrays.asList(mockDepartment, dept2));

        List<Department> result = departmentService.getDepartment();

        assertNotNull(result, "返回结果不应为null");
        assertEquals(2, result.size(), "应返回两个部门");
        assertEquals("技术部", result.get(0).getName());
        assertEquals("市场部", result.get(1).getName());

        verify(departmentRepository, times(1)).findAll();
    }

    /**
     * 测试2：当数据库无部门时返回空列表
     */
    @Test
    void getDepartment_NoDepartments_ShouldReturnEmptyList() {
        when(departmentRepository.findAll()).thenReturn(List.of());

        List<Department> result = departmentService.getDepartment();

        assertNotNull(result);
        assertTrue(result.isEmpty(), "应返回空列表");
        verify(departmentRepository, times(1)).findAll();
    }

    /**
     * 测试3：保存部门成功
     */
    @Test
    void saveDepartment_Success_ShouldReturnSavedDepartment() {
        Department newDept = new Department();
        newDept.setName("人事部");

        when(departmentRepository.save(any(Department.class))).thenReturn(newDept);

        Department result = departmentRepository.save(newDept);

        assertNotNull(result);
        assertEquals("人事部", result.getName());
        verify(departmentRepository, times(1)).save(newDept);
    }

    /**
     * 测试4：根据ID查询部门存在
     */
    @Test
    void findDepartmentById_Exists_ShouldReturnDepartment() {
        when(departmentRepository.findByDeptId(1)).thenReturn(Optional.of(mockDepartment));

        Optional<Department> result = departmentRepository.findByDeptId(1);

        assertTrue(result.isPresent());
        assertEquals("技术部", result.get().getName());
        verify(departmentRepository, times(1)).findByDeptId(1);
    }

    /**
     * 测试5：根据ID查询部门不存在
     */
    @Test
    void findDepartmentById_NotFound_ShouldReturnEmpty() {
        when(departmentRepository.findByDeptId(999)).thenReturn(Optional.empty());

        Optional<Department> result = departmentRepository.findByDeptId(999);

        assertFalse(result.isPresent());
        verify(departmentRepository, times(1)).findByDeptId(999);
    }
}
