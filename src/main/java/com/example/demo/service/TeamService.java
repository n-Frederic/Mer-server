package com.example.demo.service;

import com.example.demo.entity.Department;
import com.example.demo.entity.Team;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.service.TeamService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final DepartmentRepository departmentRepository;


    public TeamService(TeamRepository teamRepository, DepartmentRepository departmentRepository) {
        this.teamRepository = teamRepository;
        this.departmentRepository = departmentRepository;
    }

    public Team getTeamById(Long teamId) {
        Optional<Team> team = teamRepository.findById(teamId);
        return team.orElse(null);
    }

    public Optional<Department> getDepartmentByTeamId(Integer teamId) {
        // 先取 Team
        Optional<Team> teamOpt = teamRepository.findByTeamId(teamId);
        if (teamOpt.isEmpty()) {
            return Optional.empty();
        }

        Team team = teamOpt.get();

        if (team.getDepartment() == null) {
            return Optional.empty();
        }

        // 再取 Department
        Integer deptId = team.getDepartment().getDeptId();

        return departmentRepository.findByDeptId(deptId);
    }
}
