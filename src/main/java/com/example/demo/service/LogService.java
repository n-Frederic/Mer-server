package com.example.demo.service;

import com.example.demo.context.UserContext;
import com.example.demo.dto.LogRequestDTO;
import com.example.demo.dto.LogResponseDTO;
import com.example.demo.dto.LogUpdateRequest;
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

    public Page<Log> getScopedLogs(
            String mode,
            String memberIds,
            String timeFilter,
            String keyword,
            String tags,
            int page,
            int pageSize
    ) {

        Long currentUserId = UserContext.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElseThrow();

        List<Long> targetUserIds;

        switch (mode) {

            // ================= my ====================
            case "my":
                targetUserIds = List.of(currentUserId);
                break;

            // ================= member =================
            case "member":
                int role = currentUser.getRoleId();

                // 普通成员 4 不能看任何人的日志
                if (role == 4) {
                    throw new RuntimeException("权限不足：普通成员不可查看他人日志");
                }

                // CEO(1) / Admin(5) = 可看所有人日志
                if (role == 1 || role == 5) {
                    if (memberIds == null || memberIds.isBlank()) {
                        targetUserIds = userRepository.findAll()
                                .stream().map(User::getId).toList();
                    } else {
                        targetUserIds = parseIds(memberIds);
                    }
                    break;
                }

                // Manager(2) = 看本部门所有人
                if (role == 2) {
                    List<Long> deptUserIds =
                            userRepository.findByDeptId(currentUser.getDeptId())
                                    .stream().map(User::getId).toList();

                    if (memberIds == null || memberIds.isBlank()) {
                        targetUserIds = deptUserIds;
                    } else {
                        List<Long> req = parseIds(memberIds);
                        validateContain(deptUserIds, req, "成员不在你部门");
                        targetUserIds = req;
                    }
                    break;
                }

                // TeamLeader(3) = 看本团队所有人
                if (role == 3) {
                    List<Long> teamUserIds =
                            userRepository.findByTeamId(currentUser.getTeamId())
                                    .stream().map(User::getId).toList();

                    if (memberIds == null || memberIds.isBlank()) {
                        targetUserIds = teamUserIds;
                    } else {
                        List<Long> req = parseIds(memberIds);
                        validateContain(teamUserIds, req, "成员不在你团队");
                        targetUserIds = req;
                    }
                    break;
                }

            default:
                throw new RuntimeException("错误的 mode 参数");
        }

        // ======== 时间过滤参数 ========
        LocalDate start = null, end = null;
        LocalDate today = LocalDate.now();

        switch (timeFilter) {
            case "today":
                start = today; end = today; break;
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

        // ======== tags 处理 ========
        List<String> tagList = null;
        if (tags != null && !tags.isBlank()) {
            tagList = Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        // ======== 分页 ========
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // ======== 最终统一查询 ========
        return logRepository.searchLogs(
                targetUserIds,
                start,
                end,
                keyword,
                tagList,
                pageable
        );
    }

    // 工具函数
    private List<Long> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
    }

    private void validateContain(List<Long> parent, List<Long> req, String msg) {
        for (Long id : req)
            if (!parent.contains(id))
                throw new RuntimeException(msg + ": " + id);
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

    public Map<String, Object> getJournalDetail(Long logId) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("日志未找到或无权访问"));

        User author = userRepository.findById(log.getAuthor().getId()).orElse(null);
        Map<String, Object> authorInfo = Map.of(
                "user_id", author.getId(),
                "name", author.getName(),
                "email", author.getEmail()
//                "avatar_url", author.getAvatarUrl()
        );

        List<Map<String, Object>> relatedTasks = logTaskRepository.findById_LogId(logId).stream()
                .map(m -> new HashMap<String, Object>() {{
                    put("task_id", m.getTask().getId());
                    put("title", m.getTask().getTitle());
                }})
                .collect(Collectors.toList());

        List<Long> taskIds = logTaskRepository.findById_LogId(logId).stream()
                .map(m -> m.getId().getTaskId())
                .toList();

        Set<String> tags = taskIds.stream()
                .flatMap(taskId -> tagsRepository.findByTaskId(taskId).stream())
                .map(Tags::getTag)
                .collect(Collectors.toSet());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("log_id", log.getId());
        data.put("user_id", log.getAuthor().getId());
        data.put("log_date", log.getDate());
        data.put("status", log.getStatus());
        data.put("created_at", log.getCreatedAt());
        data.put("updated_at", log.getUpdatedAt());
        data.put("todaySummary", log.getSummary());
        data.put("tomorrowPlan", log.getTomorrowPlan());
        data.put("helpNeeded", log.getHelpNeeded());
        data.put("author_info", authorInfo);
        data.put("related_tasks", relatedTasks);
        data.put("tags", tags);

        return Map.of("code", 200, "message", "success", "data", data);
    }

    @Transactional
    public boolean updateLog(Long logId, LogUpdateRequest req) {
        Optional<Log> optionalLog = logRepository.findById(logId);
        if (optionalLog.isEmpty()) return false;

        Log log = optionalLog.get();

        if (req.getTodaySummary() != null) log.setSummary(req.getTodaySummary());
        if (req.getTomorrowPlan() != null) log.setTomorrowPlan(req.getTomorrowPlan());
        if (req.getHelpNeeded() != null) log.setHelpNeeded(req.getHelpNeeded());

        logRepository.save(log);

        // === 更新日志关联任务 ===
        if (req.getTaskId() != null) {
            // 删除旧关联
            logTaskRepository.deleteById_LogId(logId);

            // 新增关联
            for (Long taskId : req.getTaskId()) {
                logTaskRepository.save(new Log_Task(logId, taskId));
            }
        }

        return true;
    }


}
