```markdown
# Список эндпоинтов и заголовков Cuppa API

Автор: Walerya Pleskova  
Дата: 2025-10-07  
Базовый URL: http://localhost:9000/api

## Эндпоинты аутентификации

### POST /auth/register
**Регистрация нового пользователя**

**Заголовки:**
```http
Content-Type: application/json
```

**Тело запроса:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "first_name": "string",
  "last_name": "string"
}
```

**Ответ:**
```json
{
  "accessToken": "string",
  "tokenType": "Bearer",
  "userId": "number",
  "username": "string",
  "email": "string"
}
```

### POST /auth/login
**Вход в систему**

**Заголовки:**
```http
Content-Type: application/json
```

**Тело запроса:**
```json
{
  "email": "string",
  "password": "string"
}
```

**Ответ:**
```json
{
  "accessToken": "string",
  "tokenType": "Bearer",
  "userId": "number",
  "username": "string",
  "email": "string"
}
```

## Эндпоинты пользователей

### GET /users/me
**Получение текущего пользователя**

**Заголовки:**
```http
Content-Type: application/json
Authorization: Bearer {jwt_token}
```

**Ответ:**
```json
{
  "id": "number",
  "username": "string",
  "email": "string",
  "first_name": "string",
  "last_name": "string",
  "avatar_url": "string",
  "phone": "string",
  "is_online": "boolean",
  "last_seen": "string",
  "created_at": "string",
  "updated_at": "string"
}
```

## Административные эндпоинты

### GET /admin/users
**Получение всех пользователей**

**Заголовки:**
```http
Content-Type: application/json
```

**Ответ:**
```json
[
  {
    "id": "number",
    "username": "string",
    "email": "string",
    "first_name": "string",
    "last_name": "string",
    "avatar_url": "string",
    "phone": "string",
    "is_online": "boolean",
    "last_seen": "string",
    "created_at": "string",
    "updated_at": "string"
  }
]
```

### GET /admin/users/{id}
**Получение пользователя по ID**

**Заголовки:**
```http
Content-Type: application/json
```

**Параметры пути:**
- `id` - ID пользователя

**Ответ:**
```json
{
  "id": "number",
  "username": "string",
  "email": "string",
  "first_name": "string",
  "last_name": "string",
  "avatar_url": "string",
  "phone": "string",
  "is_online": "boolean",
  "last_seen": "string",
  "created_at": "string",
  "updated_at": "string"
}
```

### POST /admin/users
**Создание пользователя**

**Заголовки:**
```http
Content-Type: application/json
```

**Тело запроса:**
```json
{
  "username": "string",
  "email": "string",
  "first_name": "string",
  "last_name": "string",
  "avatar_url": "string",
  "phone": "string"
}
```

### PUT /admin/users/{id}
**Обновление пользователя**

**Заголовки:**
```http
Content-Type: application/json
```

**Параметры пути:**
- `id` - ID пользователя

**Тело запроса:**
```json
{
  "username": "string",
  "email": "string",
  "first_name": "string",
  "last_name": "string",
  "avatar_url": "string",
  "phone": "string",
  "is_online": "boolean"
}
```

### DELETE /admin/users/{id}
**Удаление пользователя**

**Заголовки:**
```http
Content-Type: application/json
```

**Параметры пути:**
- `id` - ID пользователя

## Общие заголовки

### Для всех запросов:
```http
Content-Type: application/json
```

### Для защищенных эндпоинтов:
```http
Content-Type: application/json
Authorization: Bearer {jwt_token}
```

## Коды ответов

- `200` - Успешный запрос
- `201` - Успешное создание
- `400` - Неверный запрос
- `401` - Неавторизован
- `404` - Не найдено
- `409` - Конфликт (дубликат)
- `500` - Ошибка сервера

## Примеры использования заголовков

### Без аутентификации:
```bash
curl -X GET http://localhost:9000/api/admin/users \
  -H "Content-Type: application/json"
```

### С аутентификацией:
```bash
curl -X GET http://localhost:9000/api/users/me \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ваш_jwt_токен"
```
```