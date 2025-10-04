# CuppaApp - Мультиплатформенный Чат

## 📋 О проекте
Мультиплатформенное чат-приложение на Spring Boot + React Native для Web, Mobile и Windows.

## 🛠 Технологии

### Backend
- **Java 21** + Spring Boot 3.x
- **PostgreSQL** - основная база данных
- **Redis Backend** (6379) - сессии, онлайн-статусы, кэш API
- **Redis Frontend** (6380) - локальный кэш для фронтенда
- **WebSocket** - реальное время
- **Maven** - сборка

### Frontend (будет разработан)
- **React Native** - кроссплатформенный UI
- **TypeScript** - типизация
- **React Native Web** - веб-версия
- **React Native Windows** - Windows приложение
- **Redis Client** - подключение к Redis Frontend

---

## 🚀 Начало работы

### 📥 Предварительные требования

#### 1. Установите необходимое ПО:

**Обязательно:**
- [Java 21](https://adoptium.net/) - Backend
- [Maven 3.6+](https://maven.apache.org/) - Сборка проекта
- [Docker Desktop](https://www.docker.com/) - База данных и Redis
- [Git](https://git-scm.com/) - Контроль версий

**Для frontend (позже):**
- [Node.js 18+](https://nodejs.org/) - JavaScript среда
- [React Native окружение](https://reactnative.dev/docs/environment-setup)

#### 2. Проверьте установку:
```bash
java -version    # Должна быть Java 21
mvn -version     # Должен быть Maven 3.6+
docker --version # Должен быть Docker
git --version    # Должен быть Git
```

---

## 🏗 Запуск проекта

### Шаг 1: Клонирование репозитория
```bash
git clone [ссылка-на-репозиторий]
cd CuppaApp
```

### Шаг 2: Запуск инфраструктуры (БД + Redis)
```bash
# Запустите базу данных и Redis в Docker
docker-compose -f docker/dev/docker-compose.dev.yml up -d
```

### Шаг 3: Проверка инфраструктуры
```bash
# Убедитесь что контейнеры запущены
docker ps

# Должны быть:
# - cuppa_postgres_dev (порт 5432)
# - cuppa_redis_backend (порт 6379) - для backend
# - cuppa_redis_frontend (порт 6380) - для frontend

# Проверьте подключение к БД
docker exec cuppa_postgres_dev psql -U cuppa_user -d cuppa_db -c "SELECT 'База готова!' as status"

# Проверьте Redis Backend
docker exec cuppa_redis_backend redis-cli ping

# Проверьте Redis Frontend
docker exec cuppa_redis_frontend redis-cli ping
```

### Шаг 4: Запуск Backend приложения
```bash
# Перейдите в папку backend и запустите приложение
cd backend
mvn spring-boot:run
```

### Шаг 5: Проверка работы
Откройте в браузере: http://localhost:8080

**Ожидаемый результат:** Белая страница с ошибкой 404 - это нормально! Приложение запущено и готово к разработке API.

---

## 🎯 Что делать дальше?

### Для Backend разработчиков:
```bash
# Работайте в папке:
cd backend/src/main/java/com/cuppa/CuppaApp/

# Создавайте:
# - Entity классы (User, Message, ChatRoom) в папке entity/
# - Repository интерфейсы в папке repository/
# - Service слой в папке service/
# - REST контроллеры в папке controller/
# - DTO в папке dto/
# - Конфигурации в папке config/
```

### Для Frontend разработчиков:
```bash
# Работайте в соответствующих папках:
cd frontend/web/      # Веб-версия (React Native Web)
cd frontend/mobile/   # Мобильные приложения (iOS/Android)
cd frontend/windows/  # Windows приложение

# Инициализируйте свои проекты:
npm init
npm install react react-native

# Подключайтесь к Redis Frontend (порт 6380) для локального кэширования
```

---

## 📁 Структура проекта

```
CuppaApp/
├── backend/              # Spring Boot приложение
│   ├── src/main/java/com/cuppa/CuppaApp/
│   │   ├── config/      # Конфигурации (WebSocket, Security)
│   │   ├── controller/  # REST + WebSocket контроллеры
│   │   ├── dto/         # Data Transfer Objects
│   │   ├── entity/      # JPA Entity (User, Message, ChatRoom)
│   │   ├── repository/  # Data Access Layer
│   │   └── service/     # Business Logic
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/            # React Native проекты
│   ├── src/            # Общие компоненты и логика
│   ├── web/            # React Native Web
│   ├── mobile/         # React Native (iOS/Android)
│   ├── windows/        # React Native Windows
│   ├── cache/          # Локальный кэш (подключение к Redis Frontend)
│   ├── package.json
│   └── tsconfig.json
├── docker/              # Docker конфигурации
│   ├── dev/            # Для разработки
│   └── prod/           # Для продакшена
├── docs/               # Документация
└── README.md
```

---

## 🏗 Архитектура кэширования

### Backend Redis (порт 6379):
- Сессии пользователей
- Онлайн-статусы в реальном времени
- Кэш частых API запросов
- WebSocket connection mapping
- Rate limiting

### Frontend Redis (порт 6380):
- Локальные сообщения (offline работа)
- Кэшированные данные пользователей
- История чатов для быстрого доступа
- Часто используемые данные
- Настройки приложения

---

## 🔧 Полезные команды

### Управление Docker:
```bash
# Запуск инфраструктуры
docker-compose -f docker/dev/docker-compose.dev.yml up -d

# Остановка
docker-compose -f docker/dev/docker-compose.dev.yml down

# Просмотр логов
docker-compose -f docker/dev/docker-compose.dev.yml logs -f

# Полная перезагрузка
docker-compose -f docker/dev/docker-compose.dev.yml down -v
docker-compose -f docker/dev/docker-compose.dev.yml up -d
```

### Разработка Backend:
```bash
# Из папки backend/
mvn clean compile      # Очистка и компиляция
mvn spring-boot:run    # Запуск приложения
mvn test              # Запуск тестов
```

### Разработка Frontend (будущие команды):
```bash
# Из папок frontend/web/, frontend/mobile/, frontend/windows/
npm install           # Установка зависимостей
npm run dev          # Запуск в режиме разработки
npm run build        # Сборка проекта
```

---

## ❌ Решение проблем

### Порт 8080 занят:
```bash
# Найдите процесс
netstat -ano | findstr :8080

# Завершите процесс (используйте реальный PID)
taskkill /PID [PID] /F

# Или запустите на другом порту
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Ошибки Docker:
```bash
# Перезапустите Docker Desktop
# Или пересоберите контейнеры
docker-compose -f docker/dev/docker-compose.dev.yml down -v
docker-compose -f docker/dev/docker-compose.dev.yml up -d
```

### Backend не видит application.properties:
```bash
# Убедитесь что запускаете из папки backend/
cd backend
mvn spring-boot:run
```

---

## 👥 Взаимодействие команд

### Backend → Frontend:
- Предоставляет REST API на `http://localhost:8080/api/`
- WebSocket для реального времени на `ws://localhost:8080/ws`
- Документация API в `docs/api-spec.yaml`

### Frontend → Backend:
- HTTP запросы к API (`http://localhost:8080/api/`)
- Подписка на WebSocket события
- Подключение к Redis Frontend (`localhost:6380`) для локального кэширования
- Локальная разработка на `http://localhost:3000` (будет настроено)

---

## 📞 Контакты и поддержка

- **Документация**: [docs/](docs/)
- **План разработки**: [docs/project-plan.md](docs/project-plan.md)
- **API спецификация**: [docs/api-spec.yaml](docs/api-spec.yaml)
- **Структура проекта**: см. раздел "Структура проекта"

---

👨‍💻 Разделение ответственности
- Backend команда (папка backend/):
- REST API endpoints
- Database entities и repositories
- Business logic и services
- WebSocket handling
- Security и authentication
- Frontend команда (папка frontend/):
- React/React Native components
- UI/UX implementation
- API integration (localhost:8080)
- Local state management
- Redis frontend caching (port 6380)

📋 Code Review процесс
- Создать Pull Request с описанием изменений
- Добавить ревьюверов из команды
- Обсудить изменения в комментариях
- Исправить замечания если нужно
- После approval - мержить в main

🚀 Рекомендации
- Частые коммиты - лучше маленькие, но частые
- Понятные сообщения - что сделано и зачем
- Тестировать локально перед пушем
- Следить за конфликтами - регулярно обновляться с main
  Понял! Полная логика workflow с develop веткой.

## 📝 Добавляем в README полный workflow:

```markdown
## 🔄 Workflow - полный процесс разработки

### 🏗 Структура веток:
- `main` - стабильная production версия (только релизы)
- `develop` - ветка разработки (интеграция всех фич)
- `feature/*` - ветки для разработки новых функций

### 🎯 Зачем это нужно:
- **main** - всегда стабилен, можно в любой момент выпустить
- **develop** - тестируем интеграцию фич перед релизом
- **feature/*** - изолированная разработка без конфликтов

## 📅 Процесс работы для команды

### 🚀 ПЕРЕД НАЧАЛОМ РАБОТЫ:

#### Backend разработчик:
```bash
# 1. Получить последнюю версию разработки
git checkout develop
git pull origin develop

# 2. Создать feature ветку ОТ develop
git checkout -b feature/backend/user-crud

# 3. Начать разработку в backend/
cd backend
mvn spring-boot:run
```

#### Frontend разработчик:
```bash
# 1. Получить последнюю версию разработки  
git checkout develop
git pull origin develop

# 2. Создать feature ветку ОТ develop
git checkout -b feature/frontend/user-interface

# 3. Начать разработку в frontend/
cd frontend/web
npm install
npm run dev
```

### 🏃‍♂️ В ПРОЦЕССЕ РАБОТЫ:

#### Backend (работает в backend/):
```bash
# Регулярно коммитить прогресс
git add .
git commit -m "feat: add user entity and repository"

# Тестировать локально
mvn test
mvn spring-boot:run

# Если нужно обновиться с develop
git fetch origin
git merge origin/develop
```

#### Frontend (работает в frontend/):
```bash
# Регулярно коммитить прогресс
git add .
git commit -m "feat: implement user list component"

# Тестировать локально
npm test
npm run dev

# Если нужно обновиться с develop
git fetch origin  
git merge origin/develop
```

### ✅ ПОСЛЕ ЗАВЕРШЕНИЯ РАБОТЫ:

#### Общий процесс:
```bash
# 1. Запушить feature ветку
git push origin feature/backend/user-crud

# 2. Создать Pull Request ИЗ feature В develop
# На GitHub: New Pull Request → from feature/... to develop

# 3. После Code Review → мержим в develop
# 4. Ветку feature можно удалить
```

## 🎯 СЦЕНАРИИ РАБОТЫ:

### Сценарий 1: Новая фича "Чат комнаты"

#### Backend:
```bash
git checkout develop
git pull origin develop
git checkout -b feature/backend/chat-rooms
# Разрабатываю API для комнат в backend/
# Создаю Room entity, RoomService, RoomController
# Тестирую: mvn spring-boot:run
# Коммиты: git commit -m "feat: add chat room functionality"
# PR: feature/backend/chat-rooms → develop
```

#### Frontend:
```bash  
git checkout develop
git pull origin develop
git checkout -b feature/frontend/chat-ui
# Разрабатываю интерфейс комнат в frontend/web/
# Создаю RoomList.jsx, RoomComponent.jsx
# Тестирую: npm run dev
# Коммиты: git commit -m "feat: implement chat room UI"
# PR: feature/frontend/chat-ui → develop
```

### Сценарий 2: Исправление бага

```bash
git checkout develop
git pull origin develop  
git checkout -b bugfix/backend/message-duplication
# Исправляю баг в backend/service/MessageService.java
# Тестирую исправление
git commit -m "fix: resolve message duplication issue"
# PR: bugfix/backend/message-duplication → develop
```

### Сценарий 3: Релиз в production

```bash
# Когда develop стабилен - создаем релиз
git checkout main
git merge develop
git tag v1.0.0
git push origin main --tags
```

## 💡 Ключевые правила:

**Все фичи:** feature/* → develop  
**Релизы:** develop → main  
**Горячие фиксы:** hotfix/* → main И develop

**Backend:** работает в `backend/`, тестирует API  
**Frontend:** работает в `frontend/`, тестирует UI
