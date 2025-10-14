#!/bin/bash
# Автоматический скрипт резервного копирования
# Сохранить как sql/backup/automated_backup_script.sh и сделать исполняемым: chmod +x automated_backup_script.sh

# Настройки
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="cuppa_db"
DB_USER="cuppa_user"
BACKUP_DIR="/backups/cuppa_app"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=30

# Создать директорию если не существует
mkdir -p $BACKUP_DIR

# Имя файла бэкапа
BACKUP_FILE="$BACKUP_DIR/cuppa_backup_$DATE.sql"

echo "Starting backup of $DB_NAME at $(date)"

# Выполнить резервное копирование
export PGPASSWORD="cuppa_pass"
pg_dump -h localhost -U cuppa_user -d cuppa_db -a -f backup.sql


# Проверить успешность выполнения
if [ $? -eq 0 ]; then
    echo "Backup completed successfully: $BACKUP_FILE"

    # Сжать бэкап
    gzip $BACKUP_FILE
    echo "Backup compressed: $BACKUP_FILE.gz"

    # Удалить старые бэкапы (старше 30 дней)
    find $BACKUP_DIR -name "*.sql.gz" -mtime +$RETENTION_DAYS -delete
    echo "Old backups older than $RETENTION_DAYS days deleted"
else
    echo "Backup failed!"
    exit 1
fi

echo "Backup process finished at $(date)"
unset PGPASSWORD