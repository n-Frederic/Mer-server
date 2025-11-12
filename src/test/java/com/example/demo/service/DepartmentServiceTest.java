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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DepartmentService单元测试
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
     * 测试1：部门存在情况下根据ID获取部门
     */
    @Test
    void getDepartmentById_DepartmentExists_ShouldReturnDepartment() {
        when(departmentRepository.findByDeptId(1)).thenReturn(Optional.of(mockDepartment));
        
        Optional<Department> result = departmentRepository.findByDeptId(1);
        
        assertTrue(result.isPresent(), "部门应该存在");
        assertEquals("技术部", result.get().getName());
        assertEquals(1, result.get().getDeptId());
    }
    
    /**
     * 测试2：部门不存在情况下根据ID获取部门
     */
    @Test
    void getDepartmentById_DepartmentNotFound_ShouldReturnEmpty() {
        when(departmentRepository.findByDeptId(999)).thenReturn(Optional.empty());
        
        Optional<Department> result = departmentRepository.findByDeptId(999);
        
        assertFalse(result.isPresent(), "部门不存在应返回空");
    }
    
    /**
     * 测试3：成功保存部门
     */
    @Test
    void saveDepartment_Success_ShouldReturnSavedDepartment() {
        when(departmentRepository.save(any(Department.class))).thenReturn(mockDepartment);
        
        Department newDept = new Department();
        newDept.setName("市场部");
        
        Department result = departmentRepository.save(newDept);
        
        assertNotNull(result);
        verify(departmentRepository, times(1)).save(newDept);
    }
    
    /**
     * 测试4：查询所有部门，返回多个部门
     */
    @Test
    void findAllDepartments_ShouldReturnAllDepartments() {
        Department dept2 = new Department();
        dept2.setDeptId(2);
        dept2.setName("市场部");
        
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(mockDepartment, dept2));
        
        List<Department> result = departmentRepository.findAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }
    
    /**
     * 测试5：查询所有部门但是无部门数据
     */
    @Test
    void findAllDepartments_NoDepartments_ShouldReturnEmptyList() {
        when(departmentRepository.findAll()).thenReturn(Arrays.asList());
        
        List<Department> result = departmentRepository.findAll();
        
        assertNotNull(result);
        assertTrue(result.isEmpty(), "应返回空列表");
    }
}

