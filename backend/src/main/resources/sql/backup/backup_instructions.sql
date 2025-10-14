-- Инструкции по резервному копированию базы данных мессенджера

-- 1. Полное резервное копирование (SQL формат)
-- Команда для выполнения в командной строке:
-- pg_dump -h localhost -U cuppa_user -d cuppa_db -F p -b -v -f backup_$(date +%Y%m%d).sql

-- 2. Резервное копирование только схемы (структуры)
-- pg_dump -h localhost -U cuppa_user -d cuppa_db -s -f schema_backup_$(date +%Y%m%d).sql

-- 3. Резервное копирование только данных
-- pg_dump -h localhost -U cuppa_user -d cuppa_db -a -f data_backup_$(date +%Y%m%d).sql

-- 4. Сжатое резервное копирование (custom формат)
-- pg_dump -h localhost -U cuppa_user -d cuppa_db -F c -b -v -f backup_$(date +%Y%m%d).backup

-- 5. Резервное копирование конкретных таблиц
-- pg_dump -h localhost -U cuppa_user -d cuppa_db -t users -t chat_rooms -t messages -f essential_tables_backup.sql

-- Параметры:
-- -h: хост
-- -U: пользователь
-- -d: база данных
-- -F p: plain text формат (SQL)
-- -F c: custom формат (сжатый)
-- -s: только схема (структура)
-- -a: только данные
-- -b: включать большие объекты
-- -v: verbose режим
-- -t: конкретные таблицы

