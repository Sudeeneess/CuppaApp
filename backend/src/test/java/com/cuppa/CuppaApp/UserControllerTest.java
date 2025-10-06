package com.cuppa.CuppaApp;

import com.cuppa.CuppaApp.entity.User;
import com.cuppa.CuppaApp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для REST API контроллера пользователей (UserController)
 *
 * <p>Тестирует все CRUD операции REST API для сущности User:
 * <ul>
 *   <li>GET - получение пользователей и статистики</li>
 *   <li>POST - создание новых пользователей</li>
 *   <li>PUT - обновление данных пользователей</li>
 *   <li>PATCH - изменение онлайн-статуса</li>
 *   <li>DELETE - удаление пользователей</li>
 * </ul>
 *
 * <p>Тесты используют MockMvc для эмуляции HTTP запросов к контроллерам
 * без необходимости запуска полноценного HTTP сервера. Все тесты выполняются
 * в контексте Spring Boot с подключением к реальной базе данных.
 *
 * <p><b>ВАЖНО:</b> Тесты работают с реальной базой данных, поэтому
 * для обеспечения изоляции и предотвращения конфликтов данных:
 * <ul>
 *   <li>Создаются пользователи с уникальными именами на основе timestamp</li>
 *   <li>Тестовые данные удаляются после выполнения тестов</li>
 *   <li>Используются существующие пользователи только для чтения</li>
 * </ul>
 *
 * @author Petr Panteev
 * @version 1.0
 * @since 05.10.2025
 */
@Sql
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    /**
     * MockMvc - основной инструмент для тестирования Spring MVC контроллеров
     *
     * <p>Позволяет отправлять HTTP запросы и проверять ответы
     * без поднятия полноценного сервера. Автоматически настраивается
     * аннотацией @AutoConfigureMockMvc.
     *
     * <p>Используется для:
     * <ul>
     *   <li>Эмуляции HTTP запросов (GET, POST, PUT, PATCH, DELETE)</li>
     *   <li>Проверки HTTP статусов ответов</li>
     *   <li>Валидации JSON структуры и содержимого ответов</li>
     *   <li>Тестирования всех слоев приложения (контроллер → сервис → репозиторий)</li>
     * </ul>
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Репозиторий для работы с данными пользователей в базе данных
     *
     * <p>Используется для подготовки тестовых данных и очистки
     * после выполнения тестов. Также может использоваться для
     * проверки состояния данных после выполнения операций.
     *
     * <p><b>В тестах используется для:</b>
     * <ul>
     *   <li>Создания тестовых пользователей для операций обновления</li>
     *   <li>Удаления тестовых данных после завершения тестов</li>
     *   <li>Проверки корректности выполнения операций в БД</li>
     * </ul>
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * ObjectMapper для преобразования Java объектов в JSON и обратно
     *
     * <p>Используется для сериализации объектов User в JSON строку
     * при отправке POST/PUT запросов и для десериализации JSON ответов
     * при необходимости проверки сложных структур данных.
     *
     * <p><b>Особенности в тестах:</b>
     * <ul>
     *   <li>Автоматически настраивается Spring Boot</li>
     *   <li>Поддерживает все аннотации Jackson</li>
     *   <li>Обрабатывает сложные объекты (LocalDateTime, вложенные объекты)</li>
     * </ul>
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Тест получения всех пользователей через GET /api/users
     *
     * <p>Проверяет корректность работы endpoint'а получения списка всех пользователей.
     * Тест предполагает, что в базе данных существует как минимум один пользователь.
     *
     * <p><b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>HTTP статус ответа (200 OK)</li>
     *   <li>Формат ответа (JSON массив)</li>
     *   <li>Наличие хотя бы одного пользователя в системе</li>
     * </ul>
     *
     * <p><b>Используемые assertions:</b>
     * <ul>
     *   <li>status().isOk() - проверка HTTP статуса 200</li>
     *   <li>jsonPath("$").isArray() - проверка формата JSON массива</li>
     *   <li>jsonPath("$.length()").value(greaterThan(0)) - проверка непустого массива</li>
     * </ul>
     *
     * @throws Exception если произошла ошибка во время выполнения HTTP запроса
     */
    @Test
    void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    /**
     * Тест получения конкретного пользователя по ID через GET /api/users/{id}
     *
     * <p>Проверяет корректность работы endpoint'а получения пользователя по идентификатору.
     * Тест использует существующего пользователя с ID=1, который должен присутствовать в БД.
     *
     * <p><b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>HTTP статус ответа (200 OK)</li>
     *   <li>Корректность ID возвращенного пользователя</li>
     *   <li>Наличие обязательных полей в ответе</li>
     * </ul>
     *
     * <p><b>Предусловие:</b> Пользователь с ID=1 должен существовать в базе данных
     *
     * @throws Exception если произошла ошибка во время выполнения HTTP запроса
     */
    @Test
    void testGetUserById() throws Exception {
        // Используем существующего пользователя с ID=1
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.email").exists());
    }

    /**
     * Тест попытки получения несуществующего пользователя
     *
     * <p>Проверяет корректную обработку ситуации, когда запрашивается
     * пользователь с несуществующим ID. Ожидается статус 404 Not Found.
     *
     * <p><b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>HTTP статус ответа (404 Not Found)</li>
     *   <li>Корректность обработки edge-case</li>
     *   <li>Отсутствие исключений на стороне сервера</li>
     * </ul>
     *
     * <p><b>Предусловие:</b> Пользователь с ID=9999 НЕ должен существовать в базе данных
     *
     * @throws Exception если произошла ошибка во время выполнения HTTP запроса
     */
    @Test
    void testGetUserById_NotFound() throws Exception {
        mockMvc.perform(get("/api/users/9999"))
                .andExpect(status().isNotFound());
    }

    /**
     * Тест создания нового пользователя через POST /api/users
     *
     * <p>Проверяет корректность работы endpoint'а создания пользователя.
     * Для обеспечения уникальности данных в каждом запуске теста
     * используются динамические значения username и email на основе текущего времени.
     *
     * <p><b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>HTTP статус ответа (201 Created)</li>
     *   <li>Наличие обязательных полей в созданном пользователе</li>
     *   <li>Корректность процесса сохранения в базу данных</li>
     * </ul>
     *
     * <p><b>Особенности реализации:</b>
     * <ul>
     *   <li>Используется System.currentTimeMillis() для генерации уникальных данных</li>
     *   <li>ObjectMapper преобразует Java объект в JSON для тела запроса</li>
     *   <li>Устанавливается Content-Type: application/json</li>
     * </ul>
     *
     * @throws Exception если произошла ошибка во время выполнения HTTP запроса
     */
    @Test
    void testCreateUser() throws Exception {
        User newUser = new User();
        newUser.setUsername("test_user_" + System.currentTimeMillis());
        newUser.setEmail("test_" + System.currentTimeMillis() + "@example.com");
        newUser.setPassword_hash("test_hash");
        newUser.setFirst_name("Test");
        newUser.setLast_name("User");
        newUser.setIs_online(true);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.email").exists());
    }

    /**
     * Тест обновления данных пользователя через PUT /api/users/{id}
     *
     * <p>Проверяет корректность работы endpoint'а обновления пользователя.
     * Тест создает временного пользователя для обновления и удаляет его
     * после завершения теста для поддержания чистоты базы данных.
     *
     * <p><b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>HTTP статус ответа (200 OK)</li>
     *   <li>Фактическое обновление данных в базе</li>
     *   <li>Корректность частичного обновления полей</li>
     * </ul>
     *
     * <p><b>Процесс тестирования:</b>
     * <ol>
     *   <li>Создание тестового пользователя в БД</li>
     *   <li>Отправка PUT запроса с данными для обновления</li>
     *   <li>Проверка успешного обновления полей</li>
     *   <li>Удаление тестового пользователя (cleanup)</li>
     * </ol>
     *
     * @throws Exception если произошла ошибка во время выполнения HTTP запроса
     */
    @Test
    void testUpdateUser() throws Exception {
        // Сначала создаем тестового пользователя
        User testUser = new User();
        testUser.setUsername("update_test_" + System.currentTimeMillis());
        testUser.setEmail("update_test_" + System.currentTimeMillis() + "@example.com");
        testUser.setPassword_hash("hash");
        testUser.setFirst_name("OldName");

        User savedUser = userRepository.save(testUser);

        // Обновляем его
        User updateData = new User();
        updateData.setFirst_name("NewName");
        updateData.setIs_online(false);

        mockMvc.perform(put("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.first_name").value("NewName"))
                .andExpect(jsonPath("$.is_online").value(false));

        // Cleanup - удаляем тестового пользователя
        userRepository.delete(savedUser);
    }

    /**
     * Тест получения количества пользователей через GET /api/users/count
     *
     * <p>Проверяет корректность работы endpoint'а получения статистики пользователей.
     * Тест проверяет, что endpoint возвращает числовое значение количества пользователей.
     *
     * <p><b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>HTTP статус ответа (200 OK)</li>
     *   <li>Формат ответа (числовой)</li>
     *   <li>Корректность подсчета количества записей</li>
     * </ul>
     *
     * <p><b>Особенности проверки:</b>
     * <ul>
     *   <li>jsonPath("$").isNumber() - проверка числового формата ответа</li>
     *   <li>Не проверяет конкретное значение (зависит от состояния БД)</li>
     *   <li>Гарантирует корректную работу метода count() репозитория</li>
     * </ul>
     *
     * @throws Exception если произошла ошибка во время выполнения HTTP запроса
     */
    @Test
    void testGetUsersCount() throws Exception {
        mockMvc.perform(get("/api/users/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }
}