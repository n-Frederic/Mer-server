-- ===========================
START TRANSACTION;
SET FOREIGN_KEY_CHECKS = 0;
-- 1. 先清空有外键依赖的关联表（避免删除主表数据时触发约束）
TRUNCATE TABLE ai_analysis_log_map;
TRUNCATE TABLE ai_analysis_task_map;
TRUNCATE TABLE role_permission;
TRUNCATE TABLE task_assignment;
TRUNCATE TABLE task_report;
TRUNCATE TABLE log_keyword;
TRUNCATE TABLE attachment;
TRUNCATE TABLE comment;
TRUNCATE TABLE notification;
TRUNCATE TABLE dashboard_item;
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

-- 角色-权限映射
INSERT INTO role_permission (id, role_id, perm_id) VALUES
  (1, 1, 1),(2, 1, 2),(3, 1, 3),(4, 1, 4),    -- Admin: all
  (5, 2, 1),(6, 2, 2),(7, 2, 3),              -- Manager: create/assign/view
  (8, 3, 3)                                   -- Member: view
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), perm_id=VALUES(perm_id);

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
INSERT INTO user
  (user_id, name, email, password, username, phone, team_id, role_id, gender, status, last_login)
VALUES
  (1001, 'Alice Admin',  '1831437770@qq.com',  '111111',  'alice',  '100-0001', 1, 5, 'F', 'active', NOW()),
  (1002, 'Bob Manager',  'bob@example.com',    '$2a$10$manager','bob',    '100-0002', 1, 2, 'M', 'active', NOW()),
  (1003, 'Carol Member', 'carol@example.com',  '$2a$10$member', 'carol',  '100-0003', 2, 4, 'F', 'active', NOW()),
  (1004, 'Lisa CEO', 'lisa@amd.com',  '$2a$10$ceo', 'lisa',  '101-0003', 1, 1, 'F', 'active', NOW()),
  (1005, 'Helen Leader', 'helen@example.com',  '$2a$10$ceo', '05hal',  '100-0013', 2, 3, 'F', 'active', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), team_id=VALUES(team_id), role_id=VALUES(role_id);

UPDATE team SET leader_id = 1001 WHERE team_id = 1;
UPDATE team SET leader_id = 1002 WHERE team_id = 2;

-- ---- 验证码 / 登录记录 ----
INSERT INTO verification_code (id, email, code, created_at) VALUES
  (1, 'bob@example.com',   '829201', NOW()),
  (2, 'carol@example.com', '553144', NOW())
ON DUPLICATE KEY UPDATE code=VALUES(code), created_at=VALUES(created_at);

INSERT INTO login (login_id, user_id, token) VALUES
  (1, 1001, 'tok_admin_abc123'),
  (2, 1002, 'tok_mgr_def456')
ON DUPLICATE KEY UPDATE token=VALUES(token);

-- ---- 任务（两套：company_task & 业务 task）----
INSERT INTO company_task
  (task_id, title, description, priority, status, startAt, dueAt)
VALUES
  (1, 'KT for new hires', 'Prepare onboarding materials', 'High',   'Reported',      NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY)),
  (2, 'Infra upgrade',    'Upgrade MySQL to 8.4',         'Medium', 'Reported',NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY))
ON DUPLICATE KEY UPDATE title=VALUES(title), status=VALUES(status);

INSERT INTO task
(task_id, title, description, creator_id, priority, status, start_at, due_at, parent_task)
VALUES
    -- 原有任务（补充 parent_task 为 NULL，表示顶级任务）
    (1, 'Release v1.0', 'Cut the first company release', 1001, 'High',   'Reported', NOW(), DATE_ADD(NOW(), INTERVAL 10 DAY), NULL),
    (2, 'Migrate CI',   'Move CI to Github Actions',     1001, 'Medium', 'Reported',  NOW(), DATE_ADD(NOW(), INTERVAL 20 DAY), NULL),

    -- 新增任务：与原有任务形成父子关系
    (3, 'Write release docs', 'Prepare release notes for v1.0', 1003, 'Medium', 'Reported', NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), 1),
    (4, 'Test CI workflow', 'Verify Github Actions pipeline', 1003, 'High', 'Assigned', NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), 2),
    (5, 'Fix CI cache issue', 'Resolve dependency cache failure', 1002, 'Urgent', 'Reported', NOW(), DATE_ADD(NOW(), INTERVAL 8 DAY), 4),

    -- 新增顶级任务（无父任务）
    (6, 'Plan v2.0 roadmap', 'Define features for next release', 1001, 'Low', 'Reported', NULL, DATE_ADD(NOW(), INTERVAL 30 DAY), NULL)
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
INSERT INTO task_report
  (report_id, task_id, reporter_id, content, attachments)
VALUES
  (1, 1, 1002, 'Initialized repo, set up pipelines.', 'README.md;pipeline.yml'),
  (2, 1, 1002, 'Fixed failing tests, coverage 82%.',  'coverage.txt')
ON DUPLICATE KEY UPDATE content=VALUES(content);

-- ---- 日志 & 关键词 ----
INSERT INTO log
  (log_id, user_id, task_id, title, content, log_date,  mood)
VALUES
  (1, 1002, 1, 'Daily Standup', 'Working on release artifacts.', CURDATE(), 'Focus'),
  (2, 1003, 2, 'Daily Standup', 'Investigating CI cache misses.', CURDATE(),  'Optimistic')
ON DUPLICATE KEY UPDATE content=VALUES(content);

INSERT INTO log_keyword (id, log_id, keyword, weight) VALUES
  (1, 1, 'release', 0.9),
  (2, 2, 'ci',      0.8)
ON DUPLICATE KEY UPDATE weight=VALUES(weight);

-- ---- 面板项 ----
INSERT INTO dashboard_item
  (item_id, scope, category, title, ref_type, ref_id, sort_order, updated_by)
VALUES
  (1, 'Company', 'Task', 'Top Priority Tasks', 'task', 1, 1, 1001),
  (2, 'Personal','Log',  'My Daily Logs',      'log',  1, 2, 1002)
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order), updated_by=VALUES(updated_by);

-- ---- AI 分析 ----
INSERT INTO ai_analysis
  (analysis_id, title, generated_at, generated_by, summary, metrics_json, suggestions)
VALUES
  (1, 'Weekly Task Analysis', NOW(), 1001, 'Overall progress is on track.',
   JSON_OBJECT('precision',0.91,'recall',0.83,'tasks',2),
   'Keep monitoring CI times; prioritize release blockers.')
ON DUPLICATE KEY UPDATE title=VALUES(title), summary=VALUES(summary);

INSERT INTO ai_analysis_log_map (id, analysis_id, log_id) VALUES
  (1, 1, 1),
  (2, 1, 2)
ON DUPLICATE KEY UPDATE analysis_id=VALUES(analysis_id), log_id=VALUES(log_id);

INSERT INTO ai_analysis_task_map (id, analysis_id, task_id) VALUES
  (1, 1, 1),
  (2, 1, 2)
ON DUPLICATE KEY UPDATE analysis_id=VALUES(analysis_id), task_id=VALUES(task_id);

-- ---- 附件 & 评论 ----
INSERT INTO attachment
  (attach_id, owner_type, owner_id, file_name, file_url, uploaded_by, uploaded_at)
VALUES
  (1, 'task', 1, 'spec-v1.pdf', 'https://files.example.com/spec-v1.pdf', 1001, NOW()),
  (2, 'ai_analysis', 1, 'weekly.pdf', 'https://files.example.com/weekly.pdf', 1001, NOW())
ON DUPLICATE KEY UPDATE file_url=VALUES(file_url);

INSERT INTO comment
  (comment_id, owner_type, owner_id, author_id, content, created_at)
VALUES
  (1, 'task', 1, 1002, 'LGTM, proceeding to next step.', NOW()),
  (2, 'task', 1, 1001, 'Please add release notes.',      NOW())
ON DUPLICATE KEY UPDATE content=VALUES(content);

-- ---- 通知 ----
INSERT INTO notification
  (notif_id, user_id, type, title, body, is_read, created_at)
VALUES
  (1, 1002, 'Task', 'New assignment',  'You were assigned to task #1', 0, NOW()),
  (2, 1003, 'Task', 'New assignment',  'You were assigned to task #2', 0, NOW()),
  (3, 1002, 'Log',  'Log reminder',    'Don''t forget to submit today''s log', 0, NOW())
ON DUPLICATE KEY UPDATE is_read=VALUES(is_read);

COMMIT;

-- 一些快速验证查询（可选）
-- SELECT COUNT(*) AS users FROM user;
-- SELECT COUNT(*) AS tasks FROM task;
-- SELECT t.task_id, t.title, a.assignee_id FROM task t JOIN task_assignment a USING(task_id);
