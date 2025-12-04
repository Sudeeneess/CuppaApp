package com.cuppa.CuppaApp.dto;

/**
 * Data Transfer Object для ответов аутентификации и регистрации.
 * Содержит данные, возвращаемые после успешного входа или регистрации пользователя.
 *
 * <p>Используется в качестве тела ответа для эндпоинтов:
 * <ul>
 *   <li>{@code POST /api/auth/register} - после успешной регистрации</li>
 *   <li>{@code POST /api/auth/login} - после успешной аутентификации</li>
 * </ul>
 * </p>
 *
 * <p>Содержит JWT-токен для доступа к защищенным ресурсам API
 * и базовую информацию о пользователе.</p>
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-06
 */
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Integer userId;
    private String username;
    private String email;

    /**
     * Конструктор для создания ответа аутентификации.
     *
     * @param accessToken JWT-токен для доступа к API
     * @param userId уникальный идентификатор пользователя
     * @username отображаемое имя пользователя
     * @param email email пользователя
     */
    public AuthResponse(String accessToken, Integer userId, String username, String email) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    /**
     * Возвращает JWT access token.
     * Используется для доступа к защищенным эндпоинтам API.
     *
     * @return JWT токен доступа
     */
    public String getAccessToken() { return accessToken; }

    /**
     * Устанавливает JWT access token.
     *
     * @param accessToken JWT токен доступа
     */
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    /**
     * Возвращает тип токена.
     * По умолчанию "Bearer" - стандартный тип для JWT-токенов.
     *
     * @return тип токена
     */
    public String getTokenType() { return tokenType; }

    /**
     * Устанавливает тип токена.
     *
     * @param tokenType тип токена
     */
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    /**
     * Возвращает уникальный идентификатор пользователя.
     * Используется для связи с другими сущностями системы.
     *
     * @return ID пользователя
     */
    public Integer getUserId() { return userId; }

    /**
     * Устанавливает уникальный идентификатор пользователя.
     *
     * @param userId ID пользователя
     */
    public void setUserId(Integer userId) { this.userId = userId; }

    /**
     * Возвращает отображаемое имя пользователя.
     * Используется в интерфейсе чата для идентификации пользователя.
     *
     * @return имя пользователя
     */
    public String getUsername() { return username; }

    /**
     * Устанавливает отображаемое имя пользователя.
     *
     * @param username имя пользователя
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Возвращает email пользователя.
     * Используется как уникальный идентификатор для входа в систему.
     *
     * @return email пользователя
     */
    public String getEmail() { return email; }

    /**
     * Устанавливает email пользователя.
     *
     * @param email email пользователя
     */
    public void setEmail(String email) { this.email = email; }
}