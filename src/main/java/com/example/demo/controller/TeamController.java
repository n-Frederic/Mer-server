package com.example.demo.controller;

import com.example.demo.entity.Team;
import com.example.demo.service.TeamService;
import org.springframework.web.bind.annotation.*;

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
}
