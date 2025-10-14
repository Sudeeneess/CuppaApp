-- Утилиты для управления пользователями и их данными

-- 1. Поиск пользователя по email или username
SELECT
    id,
    username,
    email,
    first_name,
    last_name,
    is_online,
    last_seen,
    created_at
FROM users
WHERE email ILIKE '%search_term%'
   OR username ILIKE '%search_term%'
   OR first_name ILIKE '%search_term%'
   OR last_name ILIKE '%search_term%';

-- 2. Получить полную информацию о пользователе
SELECT
    u.*,
    COUNT(DISTINCT cp.chat_id) as chat_count,
    COUNT(DISTINCT m.id) as message_count,
    MAX(m.sent_at) as last_message_date
FROM users u
LEFT JOIN chat_participants cp ON u.id = cp.user_id AND cp.is_active = true
LEFT JOIN messages m ON u.id = m.sender_id
WHERE u.id = 1  -- заменить на нужный ID
GROUP BY u.id;

-- 3. Получить все чаты пользователя
SELECT
    cr.*,
    cp.joined_at,
    cp.role,
    cp.last_read_at,
    (SELECT COUNT(*) FROM messages m WHERE m.chat_id = cr.id) as message_count
FROM chat_rooms cr
JOIN chat_participants cp ON cr.id = cp.chat_id
WHERE cp.user_id = 1  -- заменить на нужный ID
AND cp.is_active = true
ORDER BY cr.last_message_at DESC NULLS LAST;

-- 4. Получить статистику по пользователям для администратора
SELECT
    u.id,
    u.username,
    u.email,
    u.is_online,
    u.last_seen,
    u.created_at,
    COUNT(DISTINCT cp.chat_id) as active_chats,
    COUNT(DISTINCT m.id) as total_messages,
    COUNT(DISTINCT CASE WHEN m.sent_at >= NOW() - INTERVAL '7 days' THEN m.id END) as messages_7d
FROM users u
LEFT JOIN chat_participants cp ON u.id = cp.user_id AND cp.is_active = true
LEFT JOIN messages m ON u.id = m.sender_id
GROUP BY u.id, u.username, u.email, u.is_online, u.last_seen, u.created_at
ORDER BY u.created_at DESC;

-- 5. Деактивация пользователя (мягкое удаление)
-- UPDATE users
-- SET
--     is_online = false,
--     is_active = false,
--     updated_at = NOW()
-- WHERE id = 1;  -- заменить на нужный ID

-- 6. Удаление всех данных пользователя (ОПАСНО - полное удаление)
-- BEGIN;
--
-- -- Удалить сообщения пользователя
-- DELETE FROM messages WHERE sender_id = 1;
--
-- -- Удалить из участников чатов
-- DELETE FROM chat_participants WHERE user_id = 1;
--
-- -- Удалить чаты, созданные пользователем
-- DELETE FROM chat_rooms WHERE created_by = 1;
--
-- -- Удалить пользователя
-- DELETE FROM users WHERE id = 1;
--
-- COMMIT;