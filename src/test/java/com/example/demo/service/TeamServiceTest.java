package com.example.demo.service;

import com.example.demo.entity.Department;
import com.example.demo.entity.Team;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TeamService单元测试
 */
@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;
    
    @Mock
    private DepartmentRepository departmentRepository;
    
    @InjectMocks
    private TeamService teamService;
    
    private Team mockTeam;
    private Department mockDepartment;
    
    @BeforeEach
    void setUp() {
        mockDepartment = new Department();
        mockDepartment.setDeptId(1);
        mockDepartment.setName("技术部");
        
        mockTeam = new Team();
        mockTeam.setTeamId(1);
        mockTeam.setName("研发团队");
        mockTeam.setDepartment(mockDepartment);
    }
    
    /**
     * 测试1：根据ID获取团队成功
     */
    @Test
    void getTeamById_TeamExists_ShouldReturnTeam() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));
        
        Team result = teamService.getTeamById(1L);
        
        assertNotNull(result);
        assertEquals("研发团队", result.getName());
        assertEquals(1, result.getTeamId());
        
        verify(teamRepository, times(1)).findById(1L);
    }
    
    /**
     * 测试2：根据ID获取团队团队不存在
     */
    @Test
    void getTeamById_TeamNotFound_ShouldReturnNull() {
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());
        
        Team result = teamService.getTeamById(999L);
        
        assertNull(result, "团队不存在应返回null");
        
        verify(teamRepository, times(1)).findById(999L);
    }
    
    /**
     * 测试3：根据团队ID获取部门成功
     */
    @Test
    void getDepartmentByTeamId_Success_ShouldReturnDepartment() {
        when(teamRepository.findByTeamId(1)).thenReturn(Optional.of(mockTeam));
        when(departmentRepository.findByDeptId(1))
            .thenReturn(Optional.of(mockDepartment));
        
        Optional<Department> result = teamService.getDepartmentByTeamId(1);
        
        assertTrue(result.isPresent(), "应返回部门");
        assertEquals("技术部", result.get().getName());
        
        verify(teamRepository, times(1)).findByTeamId(1);
        verify(departmentRepository, times(1)).findByDeptId(1);
    }
    
    /**
     * 测试4：根据团队ID获取部门团队不存在
     */
    @Test
    void getDepartmentByTeamId_TeamNotFound_ShouldReturnEmpty() {
        when(teamRepository.findByTeamId(999)).thenReturn(Optional.empty());
        
        Optional<Department> result = teamService.getDepartmentByTeamId(999);
        
        assertFalse(result.isPresent(), "团队不存在应返回空");
        
        verify(teamRepository, times(1)).findByTeamId(999);
        verify(departmentRepository, never()).findByDeptId(any());
    }
    
    /**
     * 测试5：部门为null的情况
     */
    @Test
    void getDepartmentByTeamId_DepartmentNull_ShouldReturnEmpty() {
        Team teamWithoutDept = new Team();
        teamWithoutDept.setTeamId(1);
        teamWithoutDept.setName("研发团队");
        teamWithoutDept.setDepartment(null);  // 部门为null
        
        when(teamRepository.findByTeamId(1)).thenReturn(Optional.of(teamWithoutDept));
        
        Optional<Department> result = teamService.getDepartmentByTeamId(1);
        
        assertFalse(result.isPresent(), "部门为null应返回空");
    }
}

