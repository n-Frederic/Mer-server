package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Page<User> findByRoleIdGreaterThanAndRoleIdNot(Integer minRoleId, Integer excludeRoleId, Pageable pageable);

    // 找到同 team 的所有成员
    List<User> findByTeamId(Integer teamId);

    // 找到 team 的 leader
    List<User> findByTeamIdAndRoleId(Long teamId, Integer roleId);

    @Query("""
        SELECT u
        FROM User u
        JOIN Team t ON u.teamId = t.teamId
        WHERE (:roleId IS NULL OR u.roleId = :roleId)
          AND (:keyword IS NULL OR u.name LIKE %:keyword% OR u.email LIKE %:keyword%)
          AND (:departmentId IS NULL OR t.department.deptId = :departmentId)
          AND (:teamId IS NULL OR u.teamId = :teamId)
        """)
    Page<User> searchAssignableUsers(
            @Param("roleId") Integer roleId,
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("teamId") Long teamId,
            Pageable pageable
    );
}
