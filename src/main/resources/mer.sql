-- =========================================================
-- 建议：使用独立 schema
-- =========================================================
CREATE DATABASE IF NOT EXISTS mer DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mer;

-- 统一缺省设置
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================================
-- 工具过程：若外键不存在则添加（避免重复报错）
-- =========================================================
DROP PROCEDURE IF EXISTS add_fk_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_fk_if_not_exists(
    IN in_table VARCHAR(64),
    IN in_constraint VARCHAR(64),
    IN in_alter_sql TEXT
)
BEGIN
    DECLARE fk_cnt INT DEFAULT 0;
    SELECT COUNT(*) INTO fk_cnt
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = in_table
      AND CONSTRAINT_NAME = in_constraint;

    IF fk_cnt = 0 THEN
        SET @s = in_alter_sql;
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

-- =========================================================
-- 删除旧表（防止结构未更新）
-- =========================================================
DROP TABLE IF EXISTS
    login,
    notification,
    comment,
    attachment,
    ai_analysis,
    log,
    task_report,
    task_assignment,
    task,
    user,
    team,
    department,
    permission,
    role;

-- =========================================================
-- 基础字典 / 权限体系
-- =========================================================
CREATE TABLE role (
                      role_id       INT PRIMARY KEY AUTO_INCREMENT,
                      name          VARCHAR(100) NOT NULL,
                      description   TEXT,
                      UNIQUE KEY uq_role_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE permission (
                            perm_id       INT PRIMARY KEY AUTO_INCREMENT,
                            code          VARCHAR(100) NOT NULL,
                            name          VARCHAR(100) NOT NULL,
                            description   TEXT,
                            UNIQUE KEY uq_perm_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



-- =========================================================
-- 组织结构
-- =========================================================
CREATE TABLE department (
                            dept_id        INT PRIMARY KEY AUTO_INCREMENT,
                            name           VARCHAR(100) NOT NULL,
                            parent_dept_id INT NULL,
                            UNIQUE KEY uq_dept_name (name),
                            KEY idx_parent_dept (parent_dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE team (
                      team_id    INT PRIMARY KEY AUTO_INCREMENT,
                      name       VARCHAR(100) NOT NULL,
                      dept_id    INT NULL,
                      leader_id  BIGINT NULL,
                      UNIQUE KEY uq_team_name (name),
                      KEY idx_team_dept (dept_id),
                      KEY idx_team_leader (leader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS verification_code (
                                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                 email VARCHAR(255) NOT NULL,
    code VARCHAR(10) NOT NULL,
    created_at DATETIME NOT NULL,
    KEY idx_vc_email (email)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user (
                                    user_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    username    VARCHAR(100) NULL,          -- 用户名
    phone       VARCHAR(50) NULL,           -- 手机号
    team_id     INT NULL,                   -- 所在团队ID（外键关联team表）
    dept_id     INT NULL,
    role_id     INT NULL,                   -- 角色ID（外键关联role表）
    gender      ENUM('M', 'F') NULL, -- 性别
    birth_date  DATE NULL,                  -- 出生日期
    bio         TEXT NULL,                  -- 个人简介
    avatar_url  VARCHAR(500) NULL,          -- 头像URL
    status      ENUM('active', 'inactive', 'suspended') NOT NULL DEFAULT 'active', -- 账户状态
    last_login  DATETIME NULL,              -- 最后登录时间
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_email (email),
    KEY uq_user_username (username),
    KEY idx_user_team (team_id),
    KEY idx_user_role (role_id),
    KEY idx_user_department(dept_id),
    KEY idx_user_status (status),
    KEY idx_user_last_login (last_login)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表-完整版';
-- =========================================================
-- 任务与派发
-- =========================================================




CREATE TABLE IF NOT EXISTS `company_task` (
                                `task_id`   BIGINT NOT NULL AUTO_INCREMENT,
                                `title`     VARCHAR(255) NOT NULL,
                                `description` TEXT NULL,
                                `priority`  VARCHAR(32) NULL,
                                `status`    VARCHAR(32) NULL,
                                `startAt`   DATETIME(6) NULL,
                                `dueAt`     DATETIME(6) NULL,
                                `createdAt` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                `updatedAt` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                PRIMARY KEY (`task_id`),
                                KEY `idx_company_task_status` (`status`),
                                KEY `idx_company_task_priority` (`priority`),
                                KEY `idx_company_task_dueAt` (`dueAt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS task (
                                    task_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    title       VARCHAR(255) NOT NULL,
                                    description TEXT,
                                    creator_id  BIGINT NOT NULL,
                                    priority    ENUM('Low','Medium','High','Urgent') NOT NULL DEFAULT 'Low',
                                    status      ENUM('Published','Assigned','InProgress','Reported','Completed','Closed') NOT NULL DEFAULT 'Published',
                                    progress_pct BIGINT CHECK (progress_pct BETWEEN 0 AND 100),
                                    start_at    DATETIME NULL,
                                    due_at      DATETIME NULL,
                                    parent_task BIGINT NULL,  -- 新增父任务ID列，允许为NULL（表示无父任务）
                                    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    KEY idx_task_creator (creator_id),
                                    KEY idx_task_status (status),
                                    KEY idx_task_priority (priority),
                                    KEY idx_task_due (due_at),
                                    KEY idx_task_parent (parent_task),  -- 为父任务ID添加索引，提升查询效率
                                -- 外键约束：确保parent_task的值必须是当前表中存在的task_id
                                    CONSTRAINT fk_task_parent
                                    FOREIGN KEY (parent_task)
                                    REFERENCES task(task_id)
                                    ON DELETE SET NULL  -- 父任务被删除时，子任务的parent_task自动设为NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tags (
                                    tag_id     BIGINT NOT NULL AUTO_INCREMENT,
                                    task_id    BIGINT NOT NULL,
                                    tag        VARCHAR(100) NOT NULL,


    PRIMARY KEY (tag_id),
    -- 一个任务下同名标签只保留一条，避免重复
    UNIQUE KEY uk_task_tag (task_id, tag),
    -- 常用查询：按任务查标签
    KEY idx_tags_task (task_id),
    CONSTRAINT fk_tags_task
    FOREIGN KEY (task_id) REFERENCES task(task_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS task_assignment (
                                 assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 task_id       BIGINT NOT NULL,
                                 assignee_id   BIGINT NOT NULL,
                                 assigned_by   BIGINT NOT NULL,
                                 assigned_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 accept_at     DATETIME NULL,
                                 finish_at     DATETIME NULL,
                                 progress_pct  INT NOT NULL DEFAULT 0,

                                 CHECK (progress_pct BETWEEN 0 AND 100),
                                 KEY idx_ta_task (task_id),
                                 KEY idx_ta_assignee (assignee_id),
                                 KEY idx_ta_assigned_by (assigned_by)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS task_report (
                             report_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
                             task_id      BIGINT NOT NULL,
                             reporter_id  BIGINT NOT NULL,
                             content      TEXT,
                             address      TEXT,
                             attachments  TEXT,
                             created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             KEY idx_tr_task (task_id),
                             KEY idx_tr_reporter (reporter_id),
                             FULLTEXT KEY ftx_tr_content (content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 日志系统
-- =========================================================
CREATE TABLE IF NOT EXISTS log (
                                   log_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   user_id      BIGINT NOT NULL,

    today_summary TEXT,
    tomorrow_plan TEXT,
    help_needed   TEXT,
    status       VARCHAR(20),
    log_date     DATE NOT NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_log_user (user_id),
    KEY idx_log_date (log_date),
    FULLTEXT KEY ftx_log_content (today_summary, tomorrow_plan, help_needed)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;




CREATE TABLE IF NOT EXISTS log_task_map (
                                            log_id  BIGINT NOT NULL,
                                            task_id BIGINT NOT NULL,
                                            PRIMARY KEY (log_id, task_id),
    KEY idx_ltm_task_id (task_id),
    CONSTRAINT fk_ltm_log
    FOREIGN KEY (log_id)  REFERENCES log(log_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
    CONSTRAINT fk_ltm_task
    FOREIGN KEY (task_id) REFERENCES task(task_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 面板展示项
-- =========================================================

-- =========================================================
-- AI 分析模块
-- =========================================================
CREATE TABLE IF NOT EXISTS ai_analysis (
                             analysis_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
                             title         VARCHAR(255) NOT NULL,
                             generated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             generated_by  BIGINT NULL,
                             summary       TEXT,
                             suggestions   TEXT,
                             KEY idx_ai_generated_by (generated_by),
                             KEY idx_ai_generated_at (generated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 附件、评论、通知
-- =========================================================
CREATE TABLE IF NOT EXISTS attachment (
                            attach_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
                            owner_type   VARCHAR(50) NOT NULL,
                            owner_id     BIGINT NOT NULL,
                            file_name    VARCHAR(255) NOT NULL,
                            file_url     VARCHAR(1000) NOT NULL,
                            uploaded_by  BIGINT NOT NULL,
                            uploaded_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            KEY idx_att_owner (owner_type, owner_id),
                            KEY idx_att_uploader (uploaded_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS comment (
                         comment_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
                         owner_id    BIGINT NOT NULL,
                         log_id       BIGINT NOT NULL,
                         content      TEXT,
                         created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         KEY idx_cmt_owner (owner_id),
                         FULLTEXT KEY ftx_cmt_content (content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notification (
                              notif_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
                              user_id     BIGINT NOT NULL,
                              type        VARCHAR(50) NOT NULL,
                              relevent_id BIGINT NOT NULL,
                              title       VARCHAR(255) NOT NULL,
                              body        TEXT,
                              is_read     BOOLEAN NOT NULL DEFAULT FALSE,
                              created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              KEY idx_notif_user (user_id),
                              KEY idx_notif_is_read (is_read),
                              KEY idx_notif_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `personal_task` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
                                 `user_id` bigint(20) NOT NULL COMMENT '关联的用户ID（外键，关联user表的id）',
                                 `personal_tasks` text COMMENT '个人任务列表（JSON格式字符串存储，如["任务1","任务2"]）',
                                 PRIMARY KEY (`id`),
                                 KEY `fk_personal_task_user` (`user_id`),
                                 CONSTRAINT `fk_personal_task_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人任务表';
-- =========================================================
-- 登录表
-- =========================================================
CREATE TABLE IF NOT EXISTS login (
                       login_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '登录记录ID',
                       user_id    BIGINT NOT NULL COMMENT '用户ID，对应user表的user_id',
                       token      VARCHAR(255) NOT NULL COMMENT '登录token',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       valid_to  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '有效时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 外键约束添加
-- =========================================================
CALL add_fk_if_not_exists('department','fk_dept_parent','ALTER TABLE department ADD CONSTRAINT fk_dept_parent FOREIGN KEY (parent_dept_id) REFERENCES department(dept_id) ON UPDATE CASCADE ON DELETE SET NULL');
CALL add_fk_if_not_exists('team','fk_team_dept','ALTER TABLE team ADD CONSTRAINT fk_team_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id) ON UPDATE CASCADE ON DELETE SET NULL');
CALL add_fk_if_not_exists('user','fk_user_role','ALTER TABLE user ADD CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role(role_id) ON UPDATE CASCADE ON DELETE SET NULL');
CALL add_fk_if_not_exists('user','fk_user_team','ALTER TABLE user ADD CONSTRAINT fk_user_team FOREIGN KEY (team_id) REFERENCES team(team_id) ON UPDATE CASCADE ON DELETE SET NULL');
CALL add_fk_if_not_exists('user','fk_user_department','ALTER TABLE user ADD CONSTRAINT fk_user_department FOREIGN KEY (dept_id) REFERENCES department(dept_id) ON UPDATE CASCADE ON DELETE SET NULL');
CALL add_fk_if_not_exists('team','fk_team_leader','ALTER TABLE team ADD CONSTRAINT fk_team_leader FOREIGN KEY (leader_id) REFERENCES user(user_id) ON UPDATE CASCADE ON DELETE SET NULL');
CALL add_fk_if_not_exists('task','fk_task_creator','ALTER TABLE task ADD CONSTRAINT fk_task_creator FOREIGN KEY (creator_id) REFERENCES user(user_id) ON UPDATE CASCADE ON DELETE RESTRICT');
CALL add_fk_if_not_exists('task_assignment','fk_ta_task','ALTER TABLE task_assignment ADD CONSTRAINT fk_ta_task FOREIGN KEY (task_id) REFERENCES task(task_id) ON UPDATE CASCADE ON DELETE CASCADE');
CALL add_fk_if_not_exists('task_assignment','fk_ta_assignee','ALTER TABLE task_assignment ADD CONSTRAINT fk_ta_assignee FOREIGN KEY (assignee_id) REFERENCES user(user_id) ON UPDATE CASCADE ON DELETE RESTRICT');
CALL add_fk_if_not_exists('task_assignment','fk_ta_assigned_by','ALTER TABLE task_assignment ADD CONSTRAINT fk_ta_assigned_by FOREIGN KEY (assigned_by) REFERENCES user(user_id) ON UPDATE CASCADE ON DELETE RESTRICT');
CALL add_fk_if_not_exists('task_report','fk_tr_task','ALTER TABLE task_report ADD CONSTRAINT fk_tr_task FOREIGN KEY (task_id) REFERENCES task(task_id) ON UPDATE CASCADE ON DELETE CASCADE');
CALL add_fk_if_not_exists('task_report','fk_tr_reporter','ALTER TABLE task_report ADD CONSTRAINT fk_tr_reporter FOREIGN KEY (reporter_id) REFERENCES user(user_id) ON UPDATE CASCADE ON DELETE RESTRICT');
CALL add_fk_if_not_exists('log','fk_log_user','ALTER TABLE log ADD CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON UPDATE CASCADE ON DELETE CASCADE');

CALL add_fk_if_not_exists('ai_analysis','fk_ai_generated_by','ALTER TABLE ai_analysis ADD CONSTRAINT fk_ai_generated_by FOREIGN KEY (generated_by) REFERENCES user(user_id) ON UPDATE CASCADE ON DELETE SET NULL');
CALL add_fk_if_not_exists('attachment','fk_att_uploader','ALTER TABLE attachment ADD CONSTRAINT fk_att_uploader FOREIGN KEY (uploaded_by) REFERENCES user(user_id) ON UPDATE CASCADE ON DELETE RESTRICT');
CALL add_fk_if_not_exists('comment','fk_cmt_log','ALTER TABLE comment ADD CONSTRAINT fk_cmt_log FOREIGN KEY (log_id) REFERENCES log(log_id) ON UPDATE CASCADE ON DELETE RESTRICT');
CALL add_fk_if_not_exists('comment','fk_cmt_owner','ALTER TABLE comment ADD CONSTRAINT fk_cmt_owner FOREIGN KEY (owner_id) REFERENCES user(user_id) ON UPDATE CASCADE ON DELETE RESTRICT');
CALL add_fk_if_not_exists('notification','fk_notif_user','ALTER TABLE notification ADD CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON UPDATE CASCADE ON DELETE CASCADE');
CALL add_fk_if_not_exists('login','fk_login_user','ALTER TABLE login ADD CONSTRAINT fk_login_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON UPDATE CASCADE ON DELETE CASCADE');

SET FOREIGN_KEY_CHECKS = 1;
-- （可选）DROP PROCEDURE IF EXISTS add_fk_if_not_exists;
