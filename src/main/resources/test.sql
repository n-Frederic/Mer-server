-- ===========================
START TRANSACTION;
SET FOREIGN_KEY_CHECKS = 0;
-- 1. 先清空有外键依赖的关联表（避免删除主表数据时触发约束）
USE mer;

TRUNCATE TABLE task_assignment;
TRUNCATE TABLE task_report;
TRUNCATE TABLE log_task_map;
TRUNCATE TABLE tags;
TRUNCATE TABLE attachment;
TRUNCATE TABLE comment;
TRUNCATE TABLE notification;
TRUNCATE TABLE ai_analysis;
TRUNCATE TABLE log;
TRUNCATE TABLE task;
TRUNCATE TABLE company_task;
TRUNCATE TABLE login;
TRUNCATE TABLE verification_code;

-- 2. 清空主表（需先处理外键表，再清空主表）
-- 团队表：先重置leader_id为NULL（避免依赖用户表数据）
UPDATE team SET leader_id = NULL;
TRUNCATE TABLE team;
TRUNCATE TABLE department;
TRUNCATE TABLE user;
TRUNCATE TABLE permission;
TRUNCATE TABLE role;
SET FOREIGN_KEY_CHECKS = 1;
COMMIT;

-- ===========================
-- 第二步：插入原始种子数据（MySQL8）
-- ===========================
START TRANSACTION;
-- ---- 角色 & 权限 ----
INSERT INTO role (role_id, name, description) VALUES
  (1, 'CEO',   'CEO of the company' ),
  (2, 'Manager', 'Department manager'),
  (3, 'Team Leader',  'Team Leader'),
  (4, 'Member',  'Regular member'),
  (5, 'Admin',  'Administrator')
    
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO permission (perm_id, code, name, description) VALUES
  (1, 'TASK_CREATE',  'Create Task',  'Create tasks'),
  (2, 'TASK_ASSIGN',  'Assign Task',  'Assign tasks to users'),
  (3, 'TASK_VIEW',    'View Task',    'View tasks'),
  (4, 'USER_MANAGE',  'Manage User',  'Manage users')
ON DUPLICATE KEY UPDATE code=VALUES(code);


-- ---- 组织结构 ----
INSERT INTO department (dept_id, name, parent_dept_id) VALUES
  (1, 'Engineering', NULL),
  (2, 'Operations',  NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO team (team_id, name, dept_id, leader_id) VALUES
  (1, 'Platform', 1, NULL),
  (2, 'Ops-East', 2, NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name), dept_id=VALUES(dept_id);

-- ---- 用户（先插入，再把团队负责人回填到 team.leader_id）----
-- ---- 用户（先插入，再把团队负责人回填到 team.leader_id）----
INSERT INTO user
(user_id, name, email, password, username, phone, team_id, dept_id, role_id, gender, status, last_login)
VALUES
    -- 原5条数据（修正第3、4、5条密码格式错误，确保BCrypt加密有效性）
    (1001, 'Alice Admin',  '1831437770@qq.com',  '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'alice',  '100-0001', 1, 1, 5, 'F', 'active', NOW()), -- 明文：111111
    (1002, 'Bob Manager',  'bob@example.com',    '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'bob',    '100-0002', 1, 1, 2, 'M', 'active', NOW()), -- 明文：bob123
    (1003, 'Carol Member', 'carol@example.com',  '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'carol',  '100-0003', 2, 1, 4, 'F', 'active', NOW()), -- 明文：carol456
    (1004, 'Lisa CEO',     'lisa@amd.com',       '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'lisa',   '101-0003', 1, 2, 1, 'F', 'active', NOW()), -- 明文：lisaCEO789
    (1005, 'Helen Leader', 'helen@example.com',  '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', '05hal',  '100-0013', 2, 2, 3, 'F', 'active', NOW()), -- 明文：helenLeader2025
    -- 新增15条数据（扩展用户角色：1=CEO、2=Manager、3=Leader、4=Member、5=Admin；团队1/2，部门1/2/3）
    (1006, 'David Member', 'david@example.com',  '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'david',  '100-0006', 1, 1, 4, 'M', 'active', NOW()), -- 明文：david5678
    (1007, 'Ella Leader',  'ella@example.com',   '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'ella',   '100-0007', 2, 2, 3, 'F', 'active', NOW()), -- 明文：ellaLeader3030
    (1008, 'Frank Manager','frank@example.com',  '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'frank',  '100-0008', 1, 2, 2, 'M', 'active', NOW()), -- 明文：frankMan888
    (1009, 'Grace Member', 'grace@example.com',  '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'grace',  '100-0009', 2, 1, 4, 'F', 'active', NOW()), -- 明文：grace2025!
    (1010, 'Henry Admin',  'henry@example.com',  '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'henry',  '100-0010', 1, 2, 5, 'M', 'active', NOW()), -- 明文：henryAdmin@123
    (1011, 'Ivy Member',   'ivy@example.com',    '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'ivy',    '100-0011', 2, 2, 4, 'F', 'active', NOW()), -- 明文：ivyMember#567
    (1012, 'Jack Leader',  'jack@example.com',   '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'jack',   '100-0012', 2, 1, 3, 'M', 'active', NOW()), -- 明文：jackLead#2025
    (1013, 'Kelly Manager','kelly@example.com',  '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'kelly',  '100-0013', 2, 1, 2, 'F', 'active', NOW()), -- 明文：kellyMan$789
    (1014, 'Leo Member',   'leo@example.com',    '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'leo',    '100-0014', 2, 2, 4, 'M', 'active', NOW()), -- 明文：leo123456
    (1015, 'Mia Admin',    'mia@example.com',    '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'mia',    '100-0015', 1, 2, 5, 'F', 'active', NOW()), -- 明文：miaAdmin!23
    (1016, 'Nick Member',  'nick@example.com',   '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'nick',   '100-0016', 1, 1, 4, 'M', 'active', NOW()), -- 明文：nick567890
    (1017, 'Olivia Leader','olivia@example.com', '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'olivia', '100-0017', 1, 1, 3, 'F', 'active', NOW()), -- 明文：oliviaLead$3030
    (1018, 'Paul Manager', 'paul@example.com',   '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'paul',   '100-0018', 2, 1, 2, 'M', 'active', NOW()), -- 明文：paulMan#888
    (1019, 'Quinn Member', 'quinn@example.com',  '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'quinn',  '100-0019', 1, 2, 4, 'F', 'active', NOW()), -- 明文：quinn2025!
    (1020, 'Ryan Admin',   'ryan@example.com',   '$2a$10$FsaCNkYKalO3.3sdn/X9SOts/37p3TRV5bwm0pt.6OSS7VF34XV6K', 'ryan',   '100-0020', 1, 2, 5, 'M', 'active', NOW())  -- 明文：ryanAdmin@456
    ON DUPLICATE KEY UPDATE
                         name=VALUES(name),
                         team_id=VALUES(team_id),
                         role_id=VALUES(role_id),
                         password=VALUES(password), -- 若用户已存在，更新为新的BCrypt加密密码
                         status=VALUES(status);


UPDATE team SET leader_id = 1001 WHERE team_id = 1;
UPDATE team SET leader_id = 1002 WHERE team_id = 2;

-- ---- 验证码 / 登录记录 ----
INSERT INTO verification_code (id, email, code, created_at) VALUES
  (1, 'bob@example.com',   '829201', NOW()),
  (2, 'carol@example.com', '553144', NOW())
ON DUPLICATE KEY UPDATE code=VALUES(code), created_at=VALUES(created_at);

INSERT INTO login (login_id, user_id, token,created_at,valid_to) VALUES
  (1, 1001, 'tok_admin_abc123','2025-11-19 11:16:10','2025-11-19 11:16:10'),
  (2, 1002, '3a0c12fe-196f-4b08-bf2d-fcdcfe95973f','2025-11-19 11:16:10','2026-11-19 11:16:10')
ON DUPLICATE KEY UPDATE token=VALUES(token);

-- ---- 任务（两套：company_task & 业务 task）----
INSERT INTO company_task
  (task_id, title)
VALUES
  (1, 'KT for new hires'),
  (2, 'Infra upgrade'),
  (3, 'Infra upgrade'),
  (4, 'Infra upgrade'),
  (5, 'Infra upgrade'),
  (6, 'Infra upgrade'),
  (7, 'Infra upgrade'),
  (8, 'Infra upgrade'),
  (9, 'Infra upgrade'),
  (10, 'Infra upgrade')
ON DUPLICATE KEY UPDATE title=VALUES(title);

INSERT INTO task
(task_id, title, description, creator_id, priority, status, progress_pct,start_at, due_at, parent_task)
VALUES
    -- 原有任务（补充 parent_task 为 NULL，表示顶级任务）
    (1, 'Release v1.0', 'Cut the first company release', 1001, 'High',   'Reported',100, NOW(), DATE_ADD(NOW(), INTERVAL 10 DAY), NULL),
    (2, 'Migrate CI',   'Move CI to Github Actions',     1001, 'Medium', 'Reported', 33, NOW(), DATE_ADD(NOW(), INTERVAL 20 DAY), NULL),

    -- 新增任务：与原有任务形成父子关系
    (3, 'Write release docs', 'Prepare release notes for v1.0', 1003, 'Medium', 'Reported',44, NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), 1),
    (4, 'Test CI workflow', 'Verify Github Actions pipeline', 1003, 'High', 'Assigned', 55,NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), 2),
    (5, 'Fix CI cache issue', 'Resolve dependency cache failure', 1002, 'Urgent', 'Reported', 66,NOW(), DATE_ADD(NOW(), INTERVAL 8 DAY), 4),

    -- 新增顶级任务（无父任务）
    (6, 'Plan v2.0 roadmap', 'Define features for next release', 1001, 'Low', 'Reported', 77,NULL, DATE_ADD(NOW(), INTERVAL 30 DAY), NULL)
    ON DUPLICATE KEY UPDATE
                         title=VALUES(title),
                         status=VALUES(status),
                         parent_task=VALUES(parent_task); -- 新增：若主键冲突，同步更新 parent_task


INSERT INTO task_assignment
(assignment_id, task_id, assignee_id, assigned_by, assigned_at, progress_pct)
VALUES
    -- 原有分配记录
    (1, 1, 1002, 1001, NOW(), 20),
    (2, 2, 1003, 1001, NOW(),    0),

    -- 新增任务的分配记录（对应task_id=3、4、5、6）
    (3, 3, 1002, 1001, NOW(), 50),  -- 分配给1004处理"编写发布文档"（task_id=3）
    (4, 4, 1003, 1001, NOW(),  30),  -- 分配给1003处理"测试CI工作流"（task_id=4）
    (5, 5, 1003, 1001, NOW(), 10),  -- 分配给1003处理"修复CI缓存问题"（task_id=5）
    (6, 6, 1002, 1001, NOW(),  0)  -- 分配给1005处理"规划v2.0路线图"（task_id=6）
    ON DUPLICATE KEY UPDATE
                         progress_pct=VALUES(progress_pct),
                         assignee_id=VALUES(assignee_id);  -- 新增：冲突时同步更新负责人


INSERT INTO task_report (
    report_id,
    task_id,
    reporter_id,
    content,
    address,
    attachments,
    created_at,
    status,
    reject_reason,
    approved_by,
    approved_at,
    rejected_by,
    rejected_at
) VALUES
      -- 1. Task 1 submission report (pending review)
      (1, 1, 1002, 'Completed repo initialization and CI/CD pipeline setup', 'R&D Workstation A1', 'README.md;pipeline.yml', '2024-10-26 14:20:18', 'submitted', NULL, NULL, NULL, NULL, NULL),

      -- 2. Task 1 approved report
      (2, 1, 1002, 'Fixed failing test cases, coverage increased to 82%', 'R&D Workstation A1', 'coverage.txt', '2024-10-27 09:15:30', 'submitted', NULL, 1001, '2024-10-27 10:45:00', NULL, NULL),

      -- 3. Task 2 rejected report
      (3, 2, 1003, 'Completed basic GitHub Actions configuration', 'R&D Workstation B2', 'github-action.yml', '2024-10-28 16:30:22', 'submitted', 'CI configuration lacks cache strategy', NULL, NULL, 1001, '2024-10-28 17:10:15'),

      -- 4. Task 3 approved report
      (4, 3, 1003, 'Completed v1.0 release notes draft', 'R&D Workstation B2', 'release-notes-v1.0.md', '2024-10-29 11:20:45', 'submitted', NULL, 1002, '2024-10-29 14:30:00', NULL, NULL),

      -- 5. Task 4 submission report (pending review)
      (5, 4, 1002, 'Verified core CI workflow scenarios, 95% test cases passed', 'R&D Workstation A1', 'test-report.pdf', '2024-10-30 15:40:10', 'submitted', NULL, NULL, NULL, NULL, NULL),

      -- 6. Task 5 approved report
      (6, 5, 1002, 'Resolved dependency cache failure, build speed improved by 40%', 'R&D Workstation A1', 'cache-fix.patch', '2024-10-31 10:15:20', 'submitted', NULL, 1001, '2024-10-31 11:25:30', NULL, NULL);


INSERT INTO log
(
    log_id,
    user_id,
    today_summary,
    tomorrow_plan,
    help_needed,
    status,
    log_date
)
VALUES
    (1, 1002,
     'Working on release artifacts.',
     'Continue release packaging and smoke test.',
     NULL,
     'Focus',
     CURDATE()),

    (2, 1003,
     'Investigating CI cache misses.',
     'Re-run pipeline and compare cache keys.',
     NULL,
     'Optimistic',
     CURDATE()),
    -- Alice Admin (1001) logs
    (3, 1001,
     'Completed v1.0 release review and updated project plan documents',
     'Coordinate final pre-release checks across teams',
     'Need design team to provide new version LOGO assets',
     'Productive',
     CURDATE() - INTERVAL 1 DAY),
    (4, 1001,
     'Resolved user permission configuration issues and optimized task assignment workflow',
     'Prepare weekly meeting presentation materials and organize project risk list',
     NULL,
     'Focus',
     CURDATE() - INTERVAL 2 DAY),

    -- Bob Manager (1002) logs
    (5, 1002,
     'Reviewed team task progress and resolved resource conflicts in the Platform team',
     'Follow up on CI migration progress and sync requirements with DevOps',
     'Need backend team to cooperate with API adjustments',
     'Busy',
     CURDATE() - INTERVAL 1 DAY),
    (6, 1002,
     'Completed new employee onboarding training plan and organized technical documents',
     'Interview 3 frontend candidates and prepare evaluation reports',
     NULL,
     'Efficient',
     CURDATE() - INTERVAL 3 DAY),

    -- Carol Member (1003) logs
    (7, 1003,
     'Fixed CI cache invalidation issue and optimized build scripts',
     'Test new version compatibility and write test reports',
     'Need test environment upgrade to MySQL 8.4',
     'Challenged',
     CURDATE() - INTERVAL 1 DAY),
    (8, 1003,
     'Completed test case design for Task #4 and executed first round of testing',
     'Analyze test failure causes and submit bug reports',
     NULL,
     'Careful',
     CURDATE() - INTERVAL 2 DAY),

    -- Lisa CEO (1004) logs
    (9, 1004,
     'Attended quarterly business review meeting and confirmed next quarter goals',
     'Communicate financing progress with investors and prepare presentation materials',
     NULL,
     'Strategic',
     CURDATE() - INTERVAL 1 DAY),
    (10, 1004,
     'Approved department budget adjustment plan and optimized resource allocation',
     'Meet with department heads to understand team challenges',
     'Need finance department to provide cost analysis reports',
     'Decisive',
     CURDATE() - INTERVAL 4 DAY),

    -- Helen Leader (1005) logs
    (11, 1005,
     'Hosted Ops-East team weekly meeting and synced task progress',
     'Develop team performance evaluation standards and collect feedback',
     NULL,
     'Organized',
     CURDATE() - INTERVAL 1 DAY),
    (12, 1005,
     'Follow up on infrastructure upgrade plan and coordinate maintenance window',
     'Write team capability improvement plan and schedule training courses',
     'Need HR support for training resource coordination',
     'Proactive',
     CURDATE() - INTERVAL 3 DAY),
    (13, 1002,
     'Finalized Q3 project roadmap and distributed to all teams',
     'Kickoff meeting with design team for new feature mockups',
     'Need marketing team to align on product launch timeline',
     'Productive',
     CURDATE() - INTERVAL 5 DAY),

    (14, 1002,
     'Reviewed platform team performance metrics for last month',
     'Schedule 1:1 meetings with underperforming team members',
     NULL,
     'Focused',
     CURDATE() - INTERVAL 6 DAY),

    (15, 1002,
     'Resolved cross-department resource allocation conflict',
     'Draft resource sharing policy for future projects',
     'Legal team review needed for the policy draft',
     'Relieved',
     CURDATE() - INTERVAL 7 DAY),

    (16, 1002,
     'Completed budget review for infrastructure upgrades',
     'Present budget proposal to CFO in tomorrows meeting',
     NULL,
     'Prepared',
     CURDATE() - INTERVAL 8 DAY),

    (17, 1002,
     'Conducted root cause analysis for last weeks deployment failure',
     'Implement preventive measures in CI/CD pipeline',
     'DevOps support required for pipeline changes',
     'Determined',
     CURDATE() - INTERVAL 9 DAY)



ON DUPLICATE KEY UPDATE
                         today_summary = VALUES(today_summary),
                         tomorrow_plan = VALUES(tomorrow_plan),
                         help_needed = VALUES(help_needed),
                         status = VALUES(status),
                         log_date = VALUES(log_date);


INSERT INTO log_task_map (log_id, task_id) VALUES
                                               (3, 1),   -- log_id=3 (Alice) maps to task_id=1
                                               (3, 6),   -- log_id=3 (Alice) maps to task_id=6
                                               (5, 2),   -- log_id=5 (Bob) maps to task_id=2
                                               (7, 4),   -- log_id=7 (Carol) maps to task_id=4
                                               (7, 5),   -- log_id=7 (Carol) maps to task_id=5
                                               (9, 1),   -- log_id=9 (Lisa) maps to task_id=1
                                               (11, 2);  -- log_id=11 (Helen) maps to task_id=2

INSERT INTO tags (task_id, tag) VALUES
                                    (1, 'release'),
                                    (1, 'v1.0'),
                                    (2, 'ci'),
                                    (4, 'pipeline'),
                                    (5, 'roadmap');
--     ON DUPLICATE KEY UPDATE
--                          updated_at = updated_at;

-- ---- AI 分析 ----
INSERT INTO ai_analysis
  (analysis_id, title, generated_at, generated_by, summary, suggestions)
VALUES
  (1, 'Weekly Task Analysis', NOW(), 1001, 'Overall progress is on track.', 'Keep monitoring CI times; prioritize release blockers.')
ON DUPLICATE KEY UPDATE title=VALUES(title), summary=VALUES(summary);


-- ---- 附件 & 评论 ----
INSERT INTO attachment
  (attach_id, owner_type, owner_id, file_name, file_url, uploaded_by, uploaded_at)
VALUES
  (1, 'task', 1, 'spec-v1.pdf', 'https://files.example.com/spec-v1.pdf', 1001, NOW()),
  (2, 'ai_analysis', 1, 'weekly.pdf', 'https://files.example.com/weekly.pdf', 1001, NOW())
ON DUPLICATE KEY UPDATE file_url=VALUES(file_url);

-- Replace your existing comment inserts with this corrected version
INSERT INTO comment
(comment_id,  owner_id, log_id, content, created_at)
VALUES
    -- Use log_id=1 (exists in log table)
    (1,  1002, 1, 'LGTM, proceeding to next step.', NOW()),
    -- Use log_id=2 (exists in log table)
    (2, 1001, 2, 'Please add release notes.',      NOW()),
    -- Add new comments with valid log_ids (e.g., 3,5,7 from extended logs)
    (3, 1003, 3, 'Approved the release plan.',    NOW()),
    (4, 1004, 5, 'Can we discuss the CI timeline?', NOW())

    ON DUPLICATE KEY UPDATE content=VALUES(content);

-- ---- 通知 ----
INSERT INTO notification
  (notif_id, user_id, type,relevent_id, title, body, is_read, created_at)
VALUES
  (1, 1002, 'task', 1,'New assignment',  'You were assigned to task #1', 0, NOW()),
  (2, 1003, 'task',2, 'New assignment',  'You were assigned to task #2', 0, NOW())

ON DUPLICATE KEY UPDATE is_read=VALUES(is_read);



TRUNCATE TABLE event_log;

INSERT INTO event_log (
    event_id,
    user_id,
    event_type,
    target_id,
    target_type,
    created_at
) VALUES
      -- 1) 登录 LOGIN  （对应 login 表中的两条记录）
      (1, 1001, 'LOGIN', 1, 'login', '2025-11-19 11:16:10'),
      (2, 1002, 'LOGIN', 2, 'login', '2025-11-19 11:16:10'),

      -- 2) 任务创建 CREATE TASK  （对应 task 表中的部分任务）
      (3, 1001, 'CREATE TASK', 1, 'task', '2024-10-25 09:00:00'),
      (4, 1001, 'CREATE TASK', 2, 'task', '2024-10-25 09:05:00'),
      (5, 1003, 'CREATE TASK', 3, 'task', '2024-10-26 10:00:00'),
      (6, 1002, 'CREATE TASK', 5, 'task', '2024-10-28 09:30:00'),

      -- 3) 进度更新：> 50 / > 100 （用 task.progress_pct：4=55,5=66,6=77,1=100）
      (7, 1003, 'UPDATE PROGRESS > 50', 4, 'task', '2024-10-30 16:00:00'),  -- 55%
      (8, 1002, 'UPDATE PROGRESS > 50', 5, 'task', '2024-10-31 10:00:00'),  -- 66%
      (9, 1001, 'UPDATE PROGRESS > 50', 6, 'task', '2024-11-01 09:00:00'),  -- 77%
      (10, 1002, 'UPDATE PROGRESS > 100', 1, 'task', '2024-10-27 09:10:00'), -- 100% 视作 >100 触发

      -- 4) 创建 report CREATE REPORT （对 task_report 表中的部分记录打点）
      (11, 1002, 'CREATE REPORT', 1, 'report', '2024-10-26 14:20:18'),
      (12, 1002, 'CREATE REPORT', 2, 'report', '2024-10-27 09:15:30'),
      (13, 1003, 'CREATE REPORT', 3, 'report', '2024-10-28 16:30:22'),

      -- 5) 创建 log CREATE LOG （对 log 表中的几条日志）
      (14, 1002, 'CREATE LOG', 1, 'log', '2024-10-26 18:00:00'),
      (15, 1003, 'CREATE LOG', 2, 'log', '2024-10-26 18:05:00'),
      (16, 1001, 'CREATE LOG', 3, 'log', '2024-10-25 19:30:00'),

      -- 6) 创建 comment CREATE COMMENT （对 comment 表中的 4 条记录）
      (17, 1002, 'CREATE COMMENT', 1, 'comment', '2024-10-27 10:00:00'),
      (18, 1001, 'CREATE COMMENT', 2, 'comment', '2024-10-27 10:05:00'),
      (19, 1003, 'CREATE COMMENT', 3, 'comment', '2024-10-28 09:00:00'),
      (20, 1004, 'CREATE COMMENT', 4, 'comment', '2024-10-28 09:10:00'),

      -- 7) 重置密码 RESET PASSWORD （使用 verification_code + user 1002,1003）
      (21, 1002, 'RESET PASSWORD', 1002, 'user', '2025-11-19 11:20:00'),
      (22, 1003, 'RESET PASSWORD', 1003, 'user', '2025-11-19 11:21:00');

COMMIT;

-- 一些快速验证查询（可选）
-- SELECT COUNT(*) AS users FROM user;
-- SELECT COUNT(*) AS tasks FROM task;
-- SELECT t.task_id, t.title, a.assignee_id FROM task t JOIN task_assignment a USING(task_id);
