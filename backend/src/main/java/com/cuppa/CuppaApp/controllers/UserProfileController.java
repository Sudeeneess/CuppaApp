package com.cuppa.CuppaApp.controllers;

import com.cuppa.CuppaApp.dto.UserDto;
import com.cuppa.CuppaApp.entity.User;
import com.cuppa.CuppaApp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST контроллер для управления профилем текущего пользователя.
 *
 * <p>Предоставляет доступ к данным аутентифицированного пользователя
 * на основе JWT-токена из запроса. Все endpoints требуют действительный
 * JWT-токен и возвращают данные в безопасном DTO формате.
 *
 * <p><b>Безопасность:</b> Все методы возвращают UserDto вместо User entity,
 * что гарантирует что чувствительные данные (пароли) никогда не передаются клиенту.
 *
 * @author Walerya Pleskova
 * @since 2025-11-29
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
     * Возвращает данные текущего аутентифицированного пользователя в безопасном DTO формате.
     *
     * <p>Извлекает email пользователя из контекста безопасности Spring и возвращает
     * полную информацию о пользователе из базы данных в виде UserDto.
     *
     * <p><b>Требования:</b>
     * <ul>
     *   <li>Действительный JWT-токен в заголовке Authorization</li>
     *   <li>Пользователь должен существовать в базе данных</li>
     * </ul>
     *
     * @param authentication объект аутентификации Spring Security
     * @return данные текущего пользователя в формате UserDto
     * @throws RuntimeException если пользователь не найден в базе данных
     * @see Authentication
     * @see UserDto
     */
    @GetMapping("/me")
    public UserDto getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return UserDto.fromEntity(user);
    }

    /**
     * Возвращает список всех зарегистрированных пользователей в безопасном DTO формате.
     *
     * <p>Предоставляет доступ к базе пользователей для административных целей
     * или отладки. В production следует добавить проверки прав доступа.
     *
     * <p><b>Безопасность:</b> Возвращает UserDto вместо User entity, что исключает
     * передачу хэшей паролей клиенту.
     *
     * @return ResponseEntity со списком всех пользователей в формате UserDto
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();

            if (users.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            List<UserDto> userDtos = users.stream()
                    .map(UserDto::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}