package com.example.demo.service;

import com.example.demo.context.UserContext;
import com.example.demo.dto.LogRequestDTO;
import com.example.demo.dto.LogResponseDTO;
import com.example.demo.entity.Log;
import com.example.demo.entity.Log_Task;
import com.example.demo.entity.Tags;
import com.example.demo.entity.User;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LogService {

    private final LogRepository logRepository;
    private final TaskRepository taskRepository;
    private final LogTaskRepository logTaskRepository;
    private final UserRepository userRepository;
    private final TagsRepository tagsRepository;

    public LogService(LogRepository logRepository, TaskRepository taskRepository, LogTaskRepository logTaskRepository, UserRepository userRepository, TagsRepository tagsRepository) {
        this.logRepository = logRepository;
        this.taskRepository = taskRepository;
        this.logTaskRepository = logTaskRepository;
        this.userRepository = userRepository;
        this.tagsRepository = tagsRepository;
    }

    public Page<Log> getLogsByUser(Long authorId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));


        return logRepository.findByAuthor_Id(authorId, pageable);
    }

    @Transactional
    public LogResponseDTO createLog(LogRequestDTO request, User author) {
        Log log = new Log(
                request.getLogDate(),
                request.getSummary(),
                request.getTomorrowPlan(),
                request.getHelpNeeded(),
                author
        );

        Log saved = logRepository.save(log);
        Long logId = saved.getId();

        if (request.getTaskId() != null && !request.getTaskId().isEmpty()) {

            for (Long taskId : request.getTaskId()) {

                Log_Task relation = new Log_Task(logId, taskId);

                logTaskRepository.save(relation);
            }
        }

        String customId = String.format("L-%03d", saved.getId());

        return new LogResponseDTO(true, customId);
    }

    public List<Map<String, Object>> getTasksByLogId(Long logId) {

        List<Log_Task> mappingList = logTaskRepository.findById_LogId(logId);

        List<Map<String, Object>> tasks = new ArrayList<>();

        for (Log_Task lt : mappingList) {
            Long taskId = lt.getId().getTaskId();

            taskRepository.findById(taskId).ifPresent(task -> {
                Map<String, Object> m = new HashMap<>();
                m.put("task_id", task.getId());
                m.put("title", task.getTitle());
                tasks.add(m);
            });
        }

        return tasks;
    }

    public Page<Log> getScopedLogs(String mode, String memberIds, String timeFilter,
                                   String keyword, int page, int pageSize) {

        Long currentUserId = UserContext.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElseThrow();

        // 1. 获取要查询的用户列表 userIds
        List<Long> targetUserIds;

        switch (mode) {

            case "my":
                targetUserIds = List.of(currentUserId);
                break;

            case "member":
                if (currentUser.getRole_id() != 3) {
                    throw new RuntimeException("您没有权限查看团队成员的日志");
                }

                if (memberIds == null || memberIds.isBlank()) {
                    throw new RuntimeException("member 模式下必须提供 memberIds");
                }

                List<Long> requestedIds = Arrays.stream(memberIds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList());

                List<Long> teamMemberIds = userRepository.findByTeam_TeamId(currentUser.getTeam_id())
                        .stream()
                        .map(User::getId)
                        .toList();

                for (Long id : requestedIds) {
                    if (!teamMemberIds.contains(id)) {
                        throw new RuntimeException("非法的 memberId: " + id + "（不属于您团队）");
                    }
                }

                targetUserIds = requestedIds;
                break;

            case "approval":
                if (currentUser.getRole_id() != 3) {
                    throw new RuntimeException("无权限查看团队成员日志");
                }

                targetUserIds = userRepository.findByTeam_TeamId(currentUser.getTeam_id())
                        .stream()
                        .map(User::getId)
                        .filter(id -> !id.equals(currentUserId)) // 不包含自己
                        .collect(Collectors.toList());
                break;

            default:
                throw new RuntimeException("错误的 mode 参数");
        }

        // 2. 时间过滤
        LocalDate start = null, end = null;
        LocalDate today = LocalDate.now();

        switch (timeFilter) {
            case "today":
                start = today;
                end = today;
                break;

            case "this_week":
                start = today.with(DayOfWeek.MONDAY);
                end = today.with(DayOfWeek.SUNDAY);
                break;

            case "this_year":
                start = today.with(TemporalAdjusters.firstDayOfYear());
                end = today.with(TemporalAdjusters.lastDayOfYear());
                break;

            case "all":
            default:
                break;
        }

        PageRequest pageable = PageRequest.of(page - 1, pageSize);

        // 3. 组合筛选逻辑
        boolean useTime = start != null;
        boolean useKeyword = keyword != null && !keyword.isBlank();

        if (useTime && useKeyword) {
            return logRepository.findByAuthorIdInAndDateBetweenAndSummaryContainingIgnoreCase(
                    targetUserIds, start, end, keyword, pageable);
        }

        if (useTime) {
            return logRepository.findByAuthorIdInAndDateBetween(
                    targetUserIds, start, end, pageable);
        }

        if (useKeyword) {
            return logRepository.findByAuthorIdInAndSummaryContainingIgnoreCase(
                    targetUserIds, keyword, pageable);
        }

        // 默认情况
        return logRepository.findByAuthorIdIn(targetUserIds, pageable);
    }

    public List<Long> getTaskIdsByLogId(Long logId) {
        List<Log_Task> mappings = logTaskRepository.findById_LogId(logId);

        return mappings.stream()
                .map(m -> m.getId().getTaskId())
                .toList();
    }

    public List<String> getTagsForLog(Long logId) {

        // 查 log → task 映射表
        List<Long> taskIds = getTaskIdsByLogId(logId);

        // 去重用 Set
        Set<String> tagSet = new HashSet<>();

        for (Long taskId : taskIds) {
            List<Tags> tags = tagsRepository.findByTaskId(taskId);

            for (Tags t : tags) {
                if (t.getTag() != null) {
                    tagSet.add(t.getTag());
                }
            }
        }

        return new ArrayList<>(tagSet);
    }

    @Transactional
    public void deleteJournal(Long journalId) {

        Log log = logRepository.findById(journalId)
                .orElseThrow(() -> new NoSuchElementException("日志不存在"));

        try {
            logTaskRepository.deleteById_LogId(journalId);
            logRepository.delete(log);
        } catch (DataIntegrityViolationException e) {
            // 抛出让 Controller 捕获（比如 log_task_map 外键引用导致无法删除）
            throw e;
        }
    }


}
