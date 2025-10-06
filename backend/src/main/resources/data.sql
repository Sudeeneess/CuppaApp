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
(NULL, 'PRIVATE', 1, CURRENT_TIMESTAMP, 'Привет! Как дела?', 1, NULL, 2),
('Рабочая группа', 'GROUP', 2, CURRENT_TIMESTAMP - INTERVAL '30 minutes', 'Встреча переносится на 15:00', 3, 'Чат для обсуждения рабочих вопросов', 10),
('Друзья', 'GROUP', 1, CURRENT_TIMESTAMP - INTERVAL '1 hour', 'Кто сегодня вечером свободен?', 4, 'Чат для общения с друзьями', 15),
(NULL, 'PRIVATE', 3, CURRENT_TIMESTAMP - INTERVAL '2 hours', 'Спасибо за помощь!', 3, NULL, 2);

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
INSERT INTO messages (chat_id, sender_id, content, message_type, sent_at, delivered_at, read_at, reply_to_message_id)
VALUES
-- Сообщения в приватном чате 1
(1, 1, 'Привет! Как дела?', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '15 minutes', CURRENT_TIMESTAMP - INTERVAL '14 minutes', CURRENT_TIMESTAMP - INTERVAL '13 minutes', NULL),
(1, 2, 'Привет! Все отлично, спасибо! А у тебя?', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '10 minutes', CURRENT_TIMESTAMP - INTERVAL '9 minutes', CURRENT_TIMESTAMP - INTERVAL '8 minutes', 1),
(1, 1, 'Тоже все хорошо!', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '5 minutes', CURRENT_TIMESTAMP - INTERVAL '4 minutes', CURRENT_TIMESTAMP - INTERVAL '3 minutes', 2),

-- Сообщения в групповом чате 2 (Рабочая группа)
(2, 2, 'Доброе утро всем! Напоминаю о встрече в 14:00', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour', NULL),
(2, 3, 'Понял, спасибо!', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '50 minutes', 4),
(2, 1, 'У меня возник вопрос по проекту...', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '45 minutes', CURRENT_TIMESTAMP - INTERVAL '45 minutes', CURRENT_TIMESTAMP - INTERVAL '40 minutes', NULL),
(2, 3, 'Встреча переносится на 15:00', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP - INTERVAL '25 minutes', NULL),

-- Сообщения в групповом чате 3 (Друзья)
(3, 1, 'Всем привет!', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', NULL),
(3, 4, 'Привет! Что нового?', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour', 8),
(3, 5, '👋', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '90 minutes', CURRENT_TIMESTAMP - INTERVAL '90 minutes', CURRENT_TIMESTAMP - INTERVAL '80 minutes', NULL),
(3, 4, 'Кто сегодня вечером свободен?', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '50 minutes', NULL),

-- Сообщения в приватном чате 4
(4, 3, 'Привет! Поможешь с задачей?', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', NULL),
(4, 4, 'Конечно, в чем проблема?', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour', 13),
(4, 3, 'Спасибо за помощь!', 'TEXT', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour', 14);