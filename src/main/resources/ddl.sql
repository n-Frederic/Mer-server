INSERT INTO notification (
    user_id,
    type,
    relevent_id,
    title,
    body,
    is_read,
    created_at
)
SELECT
    ta.assignee_id        AS user_id,
    'task'                AS type,
    t.task_id             AS relevent_id,
    'DDL is 3 days later!!' AS title,
    'hurry up'            AS body,
    0                     AS is_read,      
    NOW()                 AS created_at
FROM task t
JOIN task_assignment ta
    ON ta.task_id = t.task_id
LEFT JOIN notification n
    ON n.user_id = ta.assignee_id
   AND n.type = 'task'
   AND n.relevent_id = t.task_id
WHERE
    t.due_at IS NOT NULL
    AND t.due_at BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 3 DAY)
    AND (t.progress_pct IS NULL OR t.progress_pct < 100)
    AND n.notif_id IS NULL;
