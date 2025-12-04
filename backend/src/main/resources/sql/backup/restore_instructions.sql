-- Инструкции по восстановлению базы данных из резервной копии
-- ВНИМАНИЕ: Восстановление удалит существующие данные!

-- 1. Восстановление из SQL дампа
-- psql -h localhost -U cuppa_user -d cuppa_db -f backup_file.sql

-- 2. Восстановление из custom формата
-- pg_restore -h localhost -U cuppa_user -d cuppa_db -v backup_file.backup

-- 3. Восстановление только схемы
-- psql -h localhost -U cuppa_user -d cuppa_db -f schema_backup.sql

-- 4. Восстановление только данных
-- psql -h localhost -U cuppa_user -d cuppa_db -f data_backup.sql

-- 5. Восстановление в новую базу данных
-- createdb -h localhost -U cuppa_user -d cuppa_db
-- psql -h localhost -U cuppa_user -d cuppa_db -f backup_file.sql

-- 6. Восстановление конкретных таблиц
-- pg_restore -h localhost -U cuppa_user -d cuppa_db -t users -t messages backup_file.backup

-- Процедура восстановления на продакшн:
-- 1. Остановить приложение
-- 2. Сделать резервную копию текущего состояния
-- 3. Восстановить из нужной резервной копии
-- 4. Запустить приложение
-- 5. Проверить целостность данных

-- Проверка после восстановления:
-- SELECT COUNT(*) as users_count FROM users;
-- SELECT COUNT(*) as chats_count FROM chat_rooms;
-- SELECT COUNT(*) as messages_count FROM messages;