package com.example.demo.controller;

import com.example.demo.entity.Team;
import com.example.demo.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/team")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/{teamId}")
    public Map<String, Object> getTeamName(
            @PathVariable Long teamId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            // Token 去掉 Bearer 前缀（但不验证内容）
            String token = authHeader.replace("Bearer ", "").trim();

            // 查询团队
            Team team = teamService.getTeamById(teamId);

            if (team == null) {
                return Map.of(
                        "ok", false,
                        "message", "查询编号对应团队失败"
                );
            }

            return Map.of(
                    "ok", true,
                    "team_name", team.getName()
            );

        } catch (Exception e) {
            return Map.of(
                    "ok", false,
                    "message", "查询编号对应团队失败"
            );
        }
    }

    @GetMapping("/department/{teamId}")
    public ResponseEntity<Map<String, Object>> getDepartment(
            @PathVariable Integer teamId,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        Map<String, Object> resp = new HashMap<>();

        try {
            return teamService.getDepartmentByTeamId(teamId)
                    .map(dept -> {
                        resp.put("ok", true);
                        resp.put("department_name", dept.getName());
                        return ResponseEntity.ok(resp);
                    })
                    .orElseGet(() -> {
                        resp.put("ok", false);
                        resp.put("message", "查询团队所属部门失败");
                        return ResponseEntity.ok(resp);
                    });

        } catch (Exception e) {
            resp.put("ok", false);
            resp.put("message", "查询团队所属部门失败");
            return ResponseEntity.ok(resp);
        }
    }

    @GetMapping("/teams")
    public Map<String, Object> getTeams(
            @RequestParam(value = "department_id", required = false) Integer deptId
    ) {
        List<Map<String, Object>> list = teamService.getTeams(deptId);

        return Map.of(
                "ok", true,
                "list", list
        );
    }

}
