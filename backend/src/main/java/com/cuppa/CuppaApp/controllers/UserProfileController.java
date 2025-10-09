package com.cuppa.CuppaApp.controllers;

import com.cuppa.CuppaApp.entity.User;
import com.cuppa.CuppaApp.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для управления профилем текущего пользователя.
 * Предоставляет доступ к данным аутентифицированного пользователя.
 *
 * <p>Обеспечивает получение информации о текущем пользователе
 * на основе JWT-токена из запроса.</p>
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-06
 */
@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserRepository userRepository;

    /**
     * Конструктор контроллера профиля пользователя.
     *
     * @param userRepository репозиторий для работы с данными пользователей
     */
    public UserProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Возвращает данные текущего аутентифицированного пользователя.
     *
     * <p>Извлекает имя пользователя из контекста безопасности Spring
     * и возвращает полную информацию о пользователе из базы данных.</p>
     *
     * <p><b>Важно:</b> Метод требует действительного JWT-токена в заголовке запроса.
     * Доступен только аутентифицированным пользователям.</p>
     *
     * @param authentication объект аутентификации Spring Security
     * @return данные текущего пользователя
     * @throws RuntimeException если пользователь не найден в базе данных
     * @see Authentication
     * @see User
     */
    @GetMapping("/me")
    public User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();  //

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    /**
     * Возвращает список всех зарегистрированных пользователей.
     *
     * <p>Предоставляет доступ к базе пользователей для административных целей
     * или отладки. В production следует добавить проверки прав доступа.</p>
     *
     * @return список всех пользователей системы
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();

            if (users.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}