-- Операции обслуживания базы данных мессенджера
-- ВНИМАНИЕ: Некоторые операции могут быть деструктивными! Выполнять с осторожностью.

-- 1. Очистка неактивных пользователей (не заходили более 90 дней)
-- Сначала проверить сколько будет удалено:
SELECT COUNT(*) as users_to_deactivate
FROM users
WHERE is_online = false
AND last_seen < NOW() - INTERVAL '90 days';

-- Затем выполнить деактивацию:
-- UPDATE users
-- SET is_active = false
-- WHERE is_online = false
-- AND last_seen < NOW() - INTERVAL '90 days';

-- 2. Очистка неактивных чатов (без сообщений более 180 дней)
SELECT
    cr.id,
    cr.name,
    cr.type,
    cr.last_message_at
FROM chat_rooms cr
WHERE cr.is_active = true
AND cr.last_message_at < NOW() - INTERVAL '180 days';

-- Деактивация неактивных чатов:
-- UPDATE chat_rooms
-- SET is_active = false
-- WHERE is_active = true
-- AND last_message_at < NOW() - INTERVAL '180 days';

-- 3. Поиск "осиротевших" записей
-- Сообщения от несуществующих пользователей:
SELECT COUNT(*) as orphaned_messages
FROM messages m
LEFT JOIN users u ON m.sender_id = u.id
WHERE u.id IS NULL;

-- Участники чатов с несуществующими пользователями:
SELECT COUNT(*) as orphaned_participants
FROM chat_participants cp
LEFT JOIN users u ON cp.user_id = u.id
WHERE u.id IS NULL;

-- Участники несуществующих чатов:
SELECT COUNT(*) as orphaned_participants
FROM chat_participants cp
LEFT JOIN chat_rooms cr ON cp.chat_id = cr.id
WHERE cr.id IS NULL;

-- 4. Оптимизация базы данных
-- Очистка мусора и обновление статистики
VACUUM ANALYZE;

-- Перестроение индексов (выполнять во время низкой нагрузки)
-- REINDEX DATABASE current_database();

-- 5. Статистика по длительным операциям
SELECT
    pid,
    now() - pg_stat_activity.query_start AS duration,
    query,
    state
FROM pg_stat_activity
WHERE (now() - pg_stat_activity.query_start) > interval '5 minutes'
AND datname = current_database();

-- 6. Мониторинг блокировок
SELECT
    blocked_locks.pid AS blocked_pid,
    blocking_locks.pid AS blocking_pid,
    blocked_activity.query AS blocked_query,
    blocking_activity.query AS blocking_query
FROM pg_catalog.pg_locks blocked_locks
JOIN pg_catalog.pg_stat_activity blocked_activity ON blocked_activity.pid = blocked_locks.pid
JOIN pg_catalog.pg_locks blocking_locks ON blocking_locks.locktype = blocked_locks.locktype
    AND blocking_locks.DATABASE IS NOT DISTINCT FROM blocked_locks.DATABASE
    AND blocking_locks.relation IS NOT DISTINCT FROM blocked_locks.relation
    AND blocking_locks.page IS NOT DISTINCT FROM blocked_locks.page
    AND blocking_locks.tuple IS NOT DISTINCT FROM blocked_locks.tuple
    AND blocking_locks.virtualxid IS NOT DISTINCT FROM blocked_locks.virtualxid
    AND blocking_locks.transactionid IS NOT DISTINCT FROM blocked_locks.transactionid
    AND blocking_locks.classid IS NOT DISTINCT FROM blocked_locks.classid
    AND blocking_locks.objid IS NOT DISTINCT FROM blocked_locks.objid
    AND blocking_locks.objsubid IS NOT DISTINCT FROM blocked_locks.objsubid
    AND blocking_locks.pid != blocked_locks.pid
JOIN pg_catalog.pg_stat_activity blocking_activity ON blocking_activity.pid = blocking_locks.pid
WHERE NOT blocked_locks.GRANTED;