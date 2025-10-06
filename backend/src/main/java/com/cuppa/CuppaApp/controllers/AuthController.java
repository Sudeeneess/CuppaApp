package com.cuppa.CuppaApp.controllers;

import com.cuppa.CuppaApp.dto.AuthRequest;
import com.cuppa.CuppaApp.dto.AuthResponse;
import com.cuppa.CuppaApp.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер аутентификации и регистрации пользователей.
 * Обрабатывает запросы на создание учетной записи и вход в систему.
 *
 * <p>Обеспечивает базовые операции безопасности мессенджера Cuppa,
 * возвращая JWT-токены для доступа к защищенным ресурсам.</p>
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-06
 */
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
}