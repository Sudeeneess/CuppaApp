# Инструкция по получению JWT токена

Автор: Walerya Pleskova  
Дата: 2025-10-07

## Шаг 1: Регистрация нового пользователя

### Через curl:

```bash
curl -X POST http://localhost:9000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ваш_логин",
    "email": "ваш_email@example.com",
    "password": "ваш_пароль",
    "first_name": "Ваше",
    "last_name": "Имя"
  }'
  ```

Через веб-интерфейс:

1. Откройте http://localhost:9000/api-test.html

2. Заполните поля в разделе "User Registration"

3. Нажмите "Register User"

## 2. Вход в систему и получение токена через curl:

```bash
curl -X POST http://localhost:9000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ваш_email@example.com",
    "password": "ваш_пароль"
  }'
```

Через веб-интерфейс:

1. В разделе "User Login" введите email и пароль

2. Нажмите "Login"

3. Токен автоматически сохранится и отобразится на странице

## 3. Сохранение токена

```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "ваш_логин",
  "email": "ваш_email@example.com"
}
```

Скопируйте значение accessToken

## 4. Использование токена в заголовках запросов:

```http
Authorization: Bearer ваш_токен_здесь
Content-Type: application/json
```

Пример запроса с токеном:

```bash
curl -X GET http://localhost:9000/api/users/me \
-H "Content-Type: application/json" \
-H "Authorization: Bearer ваш_токен_здесь"
```

Быстрый старт с тестовыми пользователями:
Будут созданы тестовые пользователи (пароль для всех одинаковый):
(это пример информацию обновить)

1. alice@example.com - Alice Smith
2. bob@example.com - Bob Johnson
3. carol@example.com - Carol Davis

Пример быстрого входа:

```bash
curl -X POST http://localhost:9000/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email": "alice@example.com", "password": "password123"}'
```

Автоматизация (для скриптов)
Сохранение токена в переменную:

```bash
TOKEN=$(curl -s -X POST http://localhost:9000/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email": "alice@example.com", "password": "password123"}' \
| grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

echo "Токен: $TOKEN"
```

Важная информация
___

1. ⏱️ Срок действия токена: 24 часа

2. 🔄 Обновление: Токен автоматически не обновляется

3. 🚫 Безопасность: Не передавайте токен третьим лицам

4. 📱 Хранение: Сохраняйте токен в безопасном месте

---

Что делать если токен не работает?

1. Проверьте срок действия - токен мог истечь

2. Проверьте правильность копирования - не должно быть лишних пробелов

3. Повторите вход - получите новый токен

4. Проверьте заголовок - должен быть Authorization: Bearer токен

Пример полного workflow

```bash
# 1. Регистрация
curl -X POST http://localhost:9000/api/auth/register \
-H "Content-Type: application/json" \
-d '{"username": "myuser", "email": "my@email.com", "password": "mypass"}'

# 2. Вход и сохранение токена
TOKEN=$(curl -s -X POST http://localhost:9000/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email": "my@email.com", "password": "mypass"}' \
| grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# 3. Использование токена
curl -X GET http://localhost:9000/api/users/me \
-H "Authorization: Bearer $TOKEN"
```