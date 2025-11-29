package com.cuppa.CuppaApp.controllers;

import com.cuppa.CuppaApp.dto.AuthRequest;
import com.cuppa.CuppaApp.dto.AuthResponse;
import com.cuppa.CuppaApp.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер аутентификации и регистрации пользователей.
 * Обрабатывает запросы на создание учетной записи и вход в систему.
 *
 * <p>Обеспечивает базовые операции безопасности мессенджера Cuppa,
 * возвращая JWT-токены для доступа к защищенным ресурсам.</p>
 *
 * @author Walerya Pleskova
 * @since 2025-11-29
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Конструктор контроллера аутентификации.
     *
     * @param authService сервис для обработки бизнес-логики аутентификации
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Регистрирует нового пользователя в системе.
     *
     * <p>Принимает данные пользователя, создает новую учетную запись
     * и возвращает JWT-токен для последующей аутентификации.</p>
     *
     * @param request объект с данными регистрации (имя, email, пароль)
     * @return ResponseEntity с JWT-токеном и данными пользователя
     * @see AuthRequest
     * @see AuthResponse
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Выполняет аутентификацию пользователя в системе.
     *
     * <p>Проверяет учетные данные пользователя и возвращает JWT-токен
     * для доступа к защищенным эндпоинтам приложения.</p>
     *
     * @param request объект с данными для входа (email и пароль)
     * @return ResponseEntity с JWT-токеном и данными пользователя
     * @see AuthRequest
     * @see AuthResponse
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Выполняет выход пользователя из системы.
     *
     * <p>Endpoint для корректного завершения сессии пользователя.
     * На клиентской стороне необходимо удалить JWT-токен из хранилища.
     * В текущей реализации выполняет логирование операции выхода,
     * в будущем может быть расширен до blacklist токенов.</p>
     *
     * @return ResponseEntity с подтверждением успешного выхода
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            log.info("User initiated logout procedure");

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Successfully logged out");
            responseBody.put("timestamp", LocalDateTime.now().toString());
            responseBody.put("status", "success");

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            log.error("Error during logout procedure", e);

            Map<String, String> errorBody = new HashMap<>();
            errorBody.put("error", "Logout failed");
            errorBody.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody);
        }
    }
}