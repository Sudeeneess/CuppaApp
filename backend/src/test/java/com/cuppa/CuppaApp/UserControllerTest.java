package com.cuppa.CuppaApp;

import com.cuppa.CuppaApp.entity.User;
import com.cuppa.CuppaApp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для REST API контроллера пользователей (UserController)
 *
 *
 * <p>Тесты используют MockMvc для эмуляции HTTP запросов к контроллерам
 * без необходимости запуска полноценного HTTP сервера.
 *
 *
 * @author Petr Panteev
 * @version 1.0
 * @since 05.10.2025
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    /**
     * MockMvc - основной инструмент для тестирования Spring MVC контроллеров
     *
     * <p>Позволяет отправлять HTTP запросы и проверять ответы
     * без поднятия полноценного сервера. Автоматически настраивается
     * аннотацией @AutoConfigureMockMvc.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Репозиторий для работы с пользователями в базе данных
     *
     * <p>Используется для подготовки тестовых данных и очистки
     * после выполнения тестов. Также может использоваться для
     * проверки состояния данных после выполнения операций.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * ObjectMapper для преобразования Java объектов в JSON и обратно
     *
     * <p>Используется для сериализации объектов User в JSON строку
     * при отправке POST/PUT запросов и для десериализации JSON ответов
     * при необходимости проверки сложных структур данных.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Тест получения всех пользователей через GET /api/users
     *
     * <p>Проверяет:
     * <ul>
     *   <li>Корректный HTTP статус ответа (200 OK)</li>
     *   <li>Формат ответа (JSON массив)</li>
     *   <li>Наличие хотя бы одного пользователя в ответе</li>
     * </ul>
     *
     * <p><b>Предусловие:</b> В базе данных должен существовать как минимум один пользователь
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
     * <p>Проверяет:
     * <ul>
     *   <li>Корректный HTTP статус ответа (200 OK)</li>
     *   <li>Наличие ожидаемого ID в ответе</li>
     *   <li>Наличие обязательных полей (username, email) в ответе</li>
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
     * <p>Проверяет:
     * <ul>
     *   <li>Корректный HTTP статус ответа (201 Created)</li>
     *   <li>Наличие обязательных полей в созданном пользователе</li>
     *   <li>Корректность процесса сохранения в базу данных</li>
     * </ul>
     *
     * <p><b>Особенности:</b> Для обеспечения уникальности данных в каждом запуске теста
     * используются динамические значения username и email на основе текущего времени.
     *
     * @throws Exception если произошла ошибка во время выполнения HTTP запроса
     */
    @Test
    void testCreateUser() throws Exception {
        // Создаем нового пользователя с уникальными данными
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
}