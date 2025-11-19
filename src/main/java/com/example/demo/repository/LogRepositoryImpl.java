package com.example.demo.repository;

import com.example.demo.entity.Log;
import com.example.demo.entity.Log_Task;
import com.example.demo.entity.Tags;
import com.example.demo.entity.Task;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LogRepositoryImpl implements LogRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Log> searchLogs(
            List<Long> userIds,
            LocalDate start,
            LocalDate end,
            String keyword,
            List<String> tags,
            Pageable pageable
    ) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Log> cq = cb.createQuery(Log.class);
        Root<Log> root = cq.from(Log.class);

        List<Predicate> predicates = new ArrayList<>();

        // author in (...)
        predicates.add(root.get("author").get("id").in(userIds));

        // 时间过滤
        if (start != null && end != null) {
            predicates.add(cb.between(root.get("date"), start, end));
        }

        // keyword 过滤
        if (keyword != null && !keyword.isBlank()) {
            String k = "%" + keyword.toLowerCase() + "%";
            predicates.add(
                    cb.or(
                            cb.like(cb.lower(root.get("summary")), k),
                            cb.like(cb.lower(root.get("tomorrowPlan")), k),
                            cb.like(cb.lower(root.get("helpNeeded")), k)
                    )
            );
        }

        // tags 过滤
        if (tags != null && !tags.isEmpty()) {
            Join<Log, Log_Task> logTaskJoin = root.join("logTasks", JoinType.LEFT);
            Join<Log_Task, Task> taskJoin = logTaskJoin.join("task", JoinType.LEFT);
            Join<Task, Tags> tagJoin = taskJoin.join("tags", JoinType.LEFT);

            predicates.add(tagJoin.get("tag").in(tags));

            // group by log id, having 所有 tags 都匹配
            cq.groupBy(root.get("id"));
            cq.having(cb.equal(cb.countDistinct(tagJoin.get("tag")), tags.size()));
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(cb.desc(root.get("date")));

        // ========== 查询数据 ==========
        TypedQuery<Log> query = em.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<Log> results = query.getResultList();

        // ========== 查询总数 ==========
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Log> countRoot = countQuery.from(Log.class);
        List<Predicate> countPreds = new ArrayList<>();
        countPreds.add(countRoot.get("author").get("id").in(userIds));

        if (start != null && end != null) {
            countPreds.add(cb.between(countRoot.get("date"), start, end));
        }

        if (keyword != null && !keyword.isBlank()) {
            String k = "%" + keyword.toLowerCase() + "%";
            countPreds.add(
                    cb.or(
                            cb.like(cb.lower(countRoot.get("summary")), k),
                            cb.like(cb.lower(countRoot.get("tomorrowPlan")), k),
                            cb.like(cb.lower(countRoot.get("helpNeeded")), k)
                    )
            );
        }

        if (tags != null && !tags.isEmpty()) {
            Join<Log, Log_Task> logTaskJoin = countRoot.join("logTasks", JoinType.LEFT);
            Join<Log_Task, Task> taskJoin = logTaskJoin.join("task", JoinType.LEFT);
            Join<Task, Tags> tagJoin = taskJoin.join("tags", JoinType.LEFT);

            countPreds.add(tagJoin.get("tag").in(tags));

            countQuery.groupBy(countRoot.get("id"));
            countQuery.having(cb.equal(cb.countDistinct(tagJoin.get("tag")), tags.size()));
        }

        countQuery.select(cb.countDistinct(countRoot));
        countQuery.where(cb.and(countPreds.toArray(new Predicate[0])));

        Long total = em.createQuery(countQuery)
                .getResultList().stream()
                .reduce(0L, Long::sum);

        return new PageImpl<>(results, pageable, total);
    }


}
