-- Проверка количества записей в таблицах
SELECT
    'users' as table_name,
    COUNT(*) as record_count
FROM users
UNION ALL
SELECT 'chat_rooms', COUNT(*) FROM chat_rooms
UNION ALL
SELECT 'chat_participants', COUNT(*) FROM chat_participants
UNION ALL
SELECT 'messages', COUNT(*) FROM messages;

-- Проверка нескольких пользователей
SELECT id, username, email, is_online FROM users LIMIT 5;

-- Проверка чат-комнат
SELECT id, name, type, created_by FROM chat_rooms;

-- Проверка участников чатов
SELECT
    cp.id,
    cr.name as chat_name,
    u.username as user_name,
    cp.role
FROM chat_participants cp
JOIN chat_rooms cr ON cp.chat_id = cr.id
JOIN users u ON cp.user_id = u.id
LIMIT 10;

-- Проверка сообщений
SELECT
    m.id,
    u.username as sender,
    cr.name as chat_name,
    LEFT(m.content, 50) as preview,
    m.sent_at
FROM messages m
JOIN users u ON m.sender_id = u.id
JOIN chat_rooms cr ON m.chat_id = cr.id
ORDER BY m.sent_at DESC
LIMIT 10;