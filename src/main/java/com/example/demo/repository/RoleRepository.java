package com.example.demo.repository;

import com.example.demo.entity.PersonalTask;
import com.example.demo.entity.Role;
import com.example.demo.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleId(Integer roleId);
}
