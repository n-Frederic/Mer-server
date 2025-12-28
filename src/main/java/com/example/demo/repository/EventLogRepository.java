package com.example.demo.repository;

import com.example.demo.entity.Department;
import com.example.demo.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    // 投影接口
    interface ResetPasswordAlertProjection {
        Long getUserId();
        String getUserName();
        Long getResetCount();
    }

    @Query(value = """
            SELECT 
                el.user_id AS userId,
                COALESCE(u.name, '') AS userName,
                COUNT(*) AS resetCount
            FROM event_log el
            LEFT JOIN user u ON el.user_id = u.user_id
            WHERE el.event_type = 'RESET PASSWORD'
              AND DATE(el.created_at) = :date
            GROUP BY el.user_id, u.name
            HAVING COUNT(*) > 5
            """, nativeQuery = true)
    List<ResetPasswordAlertProjection> findResetPasswordAlerts(@Param("date") LocalDate date);


    // ======== 登录趋势投影 ========
    public interface LoginTrendProjection {
        String getTimeBucket();
        Long getLoginCount();
        Long getUniqueUsers();
    }

    // 按小时
    @Query(value = """
            SELECT 
                DATE_FORMAT(el.created_at, '%Y-%m-%d %H:00:00') AS timeBucket,
                COUNT(*) AS loginCount,
                COUNT(DISTINCT el.user_id) AS uniqueUsers
            FROM event_log el
            WHERE el.event_type = 'LOGIN'
              AND el.created_at >= :startTime
              AND el.created_at < :endTime
            GROUP BY DATE_FORMAT(el.created_at, '%Y-%m-%d %H:00:00')
            ORDER BY timeBucket
            """, nativeQuery = true)
    List<LoginTrendProjection> findLoginTrendHourly(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // 按天
    @Query(value = """
            SELECT 
                DATE(el.created_at) AS timeBucket,
                COUNT(*) AS loginCount,
                COUNT(DISTINCT el.user_id) AS uniqueUsers
            FROM event_log el
            WHERE el.event_type = 'LOGIN'
              AND el.created_at >= :startTime
              AND el.created_at < :endTime
            GROUP BY DATE(el.created_at)
            ORDER BY timeBucket
            """, nativeQuery = true)
    List<LoginTrendProjection> findLoginTrendDaily(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // 按周（以 ISO 周为单位：YYYY-Www）
    @Query(value = """
            SELECT 
                DATE_FORMAT(el.created_at, '%x-W%v') AS timeBucket,
                COUNT(*) AS loginCount,
                COUNT(DISTINCT el.user_id) AS uniqueUsers
            FROM event_log el
            WHERE el.event_type = 'LOGIN'
              AND el.created_at >= :startTime
              AND el.created_at < :endTime
            GROUP BY DATE_FORMAT(el.created_at, '%x-W%v')
            ORDER BY timeBucket
            """, nativeQuery = true)
    List<LoginTrendProjection> findLoginTrendWeekly(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // ======== 日志 & 评论创建人数日变化 ========
    interface LogCommentDailyProjection {
        String getStatDate();              // '2024-10-26'
        Long getLogCreateCount();          // CREATE LOG 事件数
        Long getLogCreateUserCount();      // 创建日志的去重用户数
        Long getCommentCreateCount();      // CREATE COMMENT 事件数
        Long getCommentCreateUserCount();  // 创建评论的去重用户数
    }

    @Query(value = """
            SELECT
                DATE(el.created_at) AS statDate,
                SUM(CASE WHEN el.event_type = 'CREATE LOG' THEN 1 ELSE 0 END) AS logCreateCount,
                COUNT(DISTINCT CASE WHEN el.event_type = 'CREATE LOG' THEN el.user_id END) AS logCreateUserCount,
                SUM(CASE WHEN el.event_type = 'CREATE COMMENT' THEN 1 ELSE 0 END) AS commentCreateCount,
                COUNT(DISTINCT CASE WHEN el.event_type = 'CREATE COMMENT' THEN el.user_id END) AS commentCreateUserCount
            FROM event_log el
            WHERE el.event_type IN ('CREATE LOG','CREATE COMMENT')
              AND el.created_at >= :startTime
              AND el.created_at < :endTime
            GROUP BY DATE(el.created_at)
            ORDER BY statDate
            """, nativeQuery = true)
    List<LogCommentDailyProjection> findLogCommentDaily(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    public interface TaskDailyProjection {
        String getStatDate();               // '2024-10-26'
        Long getTaskCreateCount();          // CREATE TASK 事件数
        Long getTaskCreateUserCount();      // 创建任务的去重用户数
        Long getReportCreateCount();        // CREATE REPORT 事件数
        Long getReportCreateUserCount();    // 创建报告的去重用户数
        Long getProgressOver50Count();      // UPDATE PROGRESS > 50 事件数
        Long getProgressOver100Count();     // UPDATE PROGRESS > 100 事件数
    }

    @Query(value = """
            SELECT
                DATE(el.created_at) AS statDate,
                -- 任务创建
                SUM(CASE WHEN el.event_type = 'CREATE TASK' THEN 1 ELSE 0 END) AS taskCreateCount,
                COUNT(DISTINCT CASE WHEN el.event_type = 'CREATE TASK' THEN el.user_id END) AS taskCreateUserCount,
                -- 报告创建
                SUM(CASE WHEN el.event_type = 'CREATE REPORT' THEN 1 ELSE 0 END) AS reportCreateCount,
                COUNT(DISTINCT CASE WHEN el.event_type = 'CREATE REPORT' THEN el.user_id END) AS reportCreateUserCount,
                -- 进度更新
                SUM(CASE WHEN el.event_type = 'UPDATE PROGRESS > 50' THEN 1 ELSE 0 END) AS progressOver50Count,
                SUM(CASE WHEN el.event_type = 'UPDATE PROGRESS > 100' THEN 1 ELSE 0 END) AS progressOver100Count
            FROM event_log el
            WHERE el.event_type IN (
                      'CREATE TASK',
                      'CREATE REPORT',
                      'UPDATE PROGRESS > 50',
                      'UPDATE PROGRESS > 100'
                  )
              AND el.created_at >= :startTime
              AND el.created_at < :endTime
            GROUP BY DATE(el.created_at)
            ORDER BY statDate
            """, nativeQuery = true)
    List<TaskDailyProjection> findTaskDaily(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );


}
