package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "department")
public class Department {

    @Id
    @Column(name = "dept_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer deptId;

    private String name;

    @Column(name = "parent_dept_id")
    private Integer parentDeptId;

    public Integer getDeptId() {
        return deptId;
    }

    public String getName() {
        return name;
    }

    public Integer getParentDeptId() {
        return parentDeptId;
    }
}
