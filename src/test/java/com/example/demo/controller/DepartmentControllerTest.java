package com.example.demo.controller;

import com.example.demo.entity.Department;
import com.example.demo.interceptor.AuthInterceptor;
import com.example.demo.service.DepartmentService;
import com.example.demo.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private LoginService loginService;

    @MockBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        Mockito.when(authInterceptor.preHandle(any(), any(), any()))
                .thenReturn(true);
    }

    @Test
    @DisplayName("GET /department 返回部门列表，调用 DepartmentService.getDepartment")
    void getDepartments_success() throws Exception {
        Department d1 = Mockito.mock(Department.class);
        Department d2 = Mockito.mock(Department.class);

        Mockito.when(departmentService.getDepartment())
                .thenReturn(List.of(d1, d2));

        mockMvc.perform(get("/department"))
                .andExpect(status().isOk());

        Mockito.verify(departmentService).getDepartment();
    }
}
