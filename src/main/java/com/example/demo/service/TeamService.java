package com.example.demo.service;

import com.example.demo.entity.Department;
import com.example.demo.entity.Team;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.service.TeamService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Team team = teamRepository.findByTeamId(teamId);
//        if (teamOpt.isEmpty()) {
//            return Optional.empty();
//        }
//
//        Team team = teamOpt.get();


        if (team.getDepartment() == null) {
            return Optional.empty();
        }

        // 再取 Department
        Integer deptId = team.getDepartment().getDeptId();

        return departmentRepository.findByDeptId(deptId);
    }

    public List<Map<String, Object>> getTeams(Integer deptId) {

        List<Team> teams;

        if (deptId != null) {
            teams = teamRepository.findByDepartment_DeptId(deptId);
        } else {
            teams = teamRepository.findAll();
        }

        return teams.stream()
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("team_id", t.getTeamId());
                    map.put("name", t.getName());
                    map.put("dept_id", t.getDepartment() != null ? t.getDepartment().getDeptId() : null);
                    return map;
                })
                .collect(Collectors.toList());
    }

}
