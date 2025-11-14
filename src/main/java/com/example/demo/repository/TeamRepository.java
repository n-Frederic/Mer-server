package com.example.demo.repository;

import com.example.demo.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByLeader_Id(Long leaderId);
    Team findByTeamId(Integer teamId);
    List<Team> findByDepartment_DeptId(Integer deptId);
}
