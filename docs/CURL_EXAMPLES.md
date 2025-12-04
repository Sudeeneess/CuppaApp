# Примеры curl-запросов для Cuppa API

Автор: Walerya Pleskova  
Дата: 2025-10-07  
Базовый URL: http://localhost:9000/api

## 1. Регистрация пользователя

```bash
curl -X POST http://localhost:9000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "test@example.com", 
    "password": "password123",
    "first_name": "Test",
    "last_name": "User"
  }'
  ```

## 2. Вход в систему

```bash
curl -X POST http://localhost:9000/api/auth/login \
-H "Content-Type: application/json" \
-d '{
"email": "test@example.com",
"password": "password123"
}'
```

## 3. Получение профиля (с токеном)

```bash
curl -X GET http://localhost:9000/api/users/me \
-H "Content-Type: application/json" \
-H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 4. Получение всех пользователей

```bash
curl -X GET http://localhost:9000/api/admin/users \
-H "Content-Type: application/json"
```

## 5. Автоматизация с сохранением токена

```bash
# Сохраняем токен в переменную
TOKEN=$(curl -s -X POST http://localhost:9000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "password123"}' \
  | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# Используем токен
curl -X GET http://localhost:9000/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

## 6. Тестирование с существующими пользователями (обновите пользователей этих нет, просто как пример написала)

```bash
# Alice
curl -X POST http://localhost:9000/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email": "alice@example.com", "password": "password123"}'

# Bob
curl -X POST http://localhost:9000/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email": "bob@example.com", "password": "password123"}'

# Carol
curl -X POST http://localhost:9000/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email": "carol@example.com", "password": "password123"}'
```

## 5. Проверка ошибок

```bash
# Дублирование email (409 Conflict)
curl -X POST http://localhost:9000/api/auth/register \
-H "Content-Type: application/json" \
-d '{"username": "new_user", "email": "alice@example.com", "password": "pass"}'

# Неверный пароль (401 Unauthorized)
curl -X POST http://localhost:9000/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email": "alice@example.com", "password": "wrong"}'

# Доступ без токена (401 Unauthorized)
curl -X GET http://localhost:9000/api/users/me \
-H "Content-Type: application/json"
```
## 6. Файл коллекции: `POSTMAN_COLLECTION.json`
# Инструкция по импорту Postman коллекции
## Шаги импорта:
1. Скачайте файл `POSTMAN_COLLECTION.json`
2. Откройте Postman
3. Нажмите Import → File
4. Выберите скачанный JSON файл
5. Нажмите Import