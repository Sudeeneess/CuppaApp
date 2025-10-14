@echo off
set BACKUP_DIR=\backups\cuppa_app
set DATE=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%
set RETENTION_DAYS=30

:: Создать директорию если не существует
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

:: Имя файла бэкапа
set BACKUP_FILE=%BACKUP_DIR%\cuppa_backup_%DATE%.sql

echo Starting backup at %date% %time%

:: Выполнить резервное копирование
set PGPASSWORD=cuppa_pass
pg_dump -h localhost -U cuppa_user -d cuppa_db -f "%BACKUP_FILE%"

:: Проверить успешность выполнения
if %errorlevel% equ 0 (
    echo Backup completed successfully: %BACKUP_FILE%

    :: Сжать бэкап (требуется 7-Zip или аналоги)
    "C:\Program Files\7-Zip\7z.exe" a "%BACKUP_FILE%.7z" "%BACKUP_FILE%"
    echo Backup compressed: %BACKUP_FILE%.7z
    del "%BACKUP_FILE%"

    :: Удалить старые бэкапы (старше 30 дней)
    forfiles /p "%BACKUP_DIR%" /m "*.7z" /d -%RETENTION_DAYS% /c "cmd /c del @path"
    echo Old backups older than %RETENTION_DAYS% days deleted
) else (
    echo Backup failed!
    exit /b 1
)

echo Backup process finished at %date% %time%