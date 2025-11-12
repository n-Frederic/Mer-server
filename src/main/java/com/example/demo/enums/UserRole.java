package com.example.demo.enums;

public enum UserRole {
    CEO(1),
    MANAGER(2),
    TEAM_LEADER(3),
    MEMBER(4),
    ADMIN(5);

    private final int id;

    UserRole(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    // 获取当前角色允许分配任务的下一级角色
    public static Integer getAssignableRoleId(Integer currentRoleId) {
        if (currentRoleId == null) return null;
        return switch (currentRoleId) {
            case 1 -> MANAGER.getId();
            case 2 -> TEAM_LEADER.getId();
            case 3 -> MEMBER.getId();
            default -> null; // Member、Admin 无法分配任务
        };
    }
}
