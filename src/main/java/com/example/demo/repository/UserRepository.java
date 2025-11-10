package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Page<User> findByRoleIdGreaterThanAndRoleIdNot(Integer minRoleId, Integer excludeRoleId, Pageable pageable);

    // 找到同 team 的所有成员
    List<User> findByTeamId(Integer teamId);

    // 找到 team 的 leader
    List<User> findByTeamIdAndRoleId(Long teamId, Integer roleId);
}
