-- ОЧИСТКА ДАННЫХ (в правильном порядке из-за внешних ключей)
DELETE FROM messages;
DELETE FROM chat_participants;
DELETE FROM chat_rooms;
DELETE FROM users;

-- Сброс последовательностей (чтобы ID начинались с 1 при новых вставках)
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE chat_rooms_id_seq RESTART WITH 1;
ALTER SEQUENCE chat_participants_id_seq RESTART WITH 1;
ALTER SEQUENCE messages_id_seq RESTART WITH 1;

TRUNCATE TABLE chat_participants, chat_rooms, users RESTART IDENTITY CASCADE;
-- 1. Заполняем таблицу пользователей
INSERT INTO users (username, email, password_hash, first_name, last_name, avatar_url, phone, is_online, last_seen)
VALUES
('alice_smith', 'alice@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'Alice', 'Smith', 'https://example.com/avatars/alice.jpg', '+1234567890', true, CURRENT_TIMESTAMP),
('bob_johnson', 'bob@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'Bob', 'Johnson', 'https://example.com/avatars/bob.jpg', '+1234567891', false, CURRENT_TIMESTAMP - INTERVAL '1 hour'),
('carol_davis', 'carol@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'Carol', 'Davis', 'https://example.com/avatars/carol.jpg', '+1234567892', true, CURRENT_TIMESTAMP),
('david_wilson', 'david@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'David', 'Wilson', NULL, '+1234567893', false, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('emma_brown', 'emma@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'Emma', 'Brown', 'https://example.com/avatars/emma.jpg', NULL, true, CURRENT_TIMESTAMP);

-- 2. Заполняем таблицу чат-комнат
INSERT INTO chat_rooms (name, type, created_by, last_message_at, last_message_text, last_message_sender_id, description, max_participants)
VALUES
(NULL, 'PRIVATE', 1, CURRENT_TIMESTAMP, 'Test12', 1, NULL, 2),
('Work group', 'GROUP', 2, CURRENT_TIMESTAMP - INTERVAL '30 minutes', 'Test13', 3, 'test14', 10),
('Friends', 'GROUP', 1, CURRENT_TIMESTAMP - INTERVAL '1 hour', 'Test15', 4, 'Test16', 15),
(NULL, 'PRIVATE', 3, CURRENT_TIMESTAMP - INTERVAL '2 hours', 'Test17', 3, NULL, 2);

-- 3. Заполняем таблицу участников чатов
INSERT INTO chat_participants (chat_id, user_id, role, last_read_at)
VALUES
-- Участники приватного чата 1 (Alice и Bob)
(1, 1, 'MEMBER', CURRENT_TIMESTAMP),
(1, 2, 'MEMBER', CURRENT_TIMESTAMP - INTERVAL '5 minutes'),
-- Участники группового чата 2 (Рабочая группа)
(2, 1, 'MEMBER', CURRENT_TIMESTAMP),
(2, 2, 'ADMIN', CURRENT_TIMESTAMP),
(2, 3, 'MEMBER', CURRENT_TIMESTAMP - INTERVAL '10 minutes'),
(2, 4, 'MEMBER', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
-- Участники группового чата 3 (Друзья)
(3, 1, 'ADMIN', CURRENT_TIMESTAMP),
(3, 3, 'MEMBER', CURRENT_TIMESTAMP),
(3, 4, 'MEMBER', CURRENT_TIMESTAMP),
(3, 5, 'MEMBER', CURRENT_TIMESTAMP - INTERVAL '30 minutes'),
-- Участники приватного чата 4 (Carol и David)
(4, 3, 'MEMBER', CURRENT_TIMESTAMP),
(4, 4, 'MEMBER', CURRENT_TIMESTAMP);

-- 4. Заполняем таблицу сообщений
INSERT INTO messages (id, chat_id, sender_id, content, message_type, sent_at, delivered_at, read_at, reply_to_message_id)
VALUES
-- Сообщения в приватном чате 1
(1, 1, 1, 'Hi! you fine?', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '15 minutes', CURRENT_TIMESTAMP - INTERVAL '14 minutes', CURRENT_TIMESTAMP - INTERVAL '13 minutes', NULL),
(2, 1, 2, 'HI! All good, thx! You?', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '10 minutes', CURRENT_TIMESTAMP - INTERVAL '9 minutes', CURRENT_TIMESTAMP - INTERVAL '8 minutes', 1),
(3, 1, 1, 'I to!', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '5 minutes', CURRENT_TIMESTAMP - INTERVAL '4 minutes', CURRENT_TIMESTAMP - INTERVAL '3 minutes', 2),
-- Сообщения в групповом чате 2 (Рабочая группа)
(4, 2, 2, 'Test1', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour', NULL),
(5, 2, 3, 'Test2', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '50 minutes', 4),
(6, 2, 1, 'Test3', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '45 minutes', CURRENT_TIMESTAMP - INTERVAL '45 minutes', CURRENT_TIMESTAMP - INTERVAL '40 minutes', NULL),
(7, 2, 3, 'Test4', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP - INTERVAL '25 minutes', NULL),
-- Сообщения в групповом чате 3 (Друзья)
(8, 3, 1, 'Test5', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', NULL),
(9, 3, 4, 'TEst6', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour', 8),
(10, 3, 5, 'Test7', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '90 minutes', CURRENT_TIMESTAMP - INTERVAL '90 minutes', CURRENT_TIMESTAMP - INTERVAL '80 minutes', NULL),
(11, 3, 4, 'Test8', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '50 minutes', NULL),
-- Сообщения в приватном чате 4
(12, 4, 3, 'Test9', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', NULL),
(13, 4, 4, 'Test10', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour', 12), -- Исправлено: было 13
(14, 4, 3, 'Test11', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour', 13); -- Исправлено: было 14