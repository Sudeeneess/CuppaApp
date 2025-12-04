package com.cuppa.CuppaApp.dto;

/**
 * Data Transfer Object для запросов аутентификации и регистрации.
 * Содержит данные, необходимые для создания учетной записи или входа в систему.
 *
 * <p>Используется в качестве тела запроса для эндпоинтов:
 * <ul>
 *   <li>{@code POST /api/auth/register} - регистрация нового пользователя</li>
 *   <li>{@code POST /api/auth/login} - аутентификация существующего пользователя</li>
 * </ul>
 *
 * <p><b>Примечание:</b> Для регистрации обычно требуются все поля,
 * для входа - только email и password.</p>
 *
 * @author Walerya Pleskova
 * @since 2025-11-29
 */
public class AuthRequest {
    private String email;
    private String username;
    private String password;
    private String first_name;
    private String last_name;

    /**
     * Конструктор по умолчанию.
     * Требуется для десериализации JSON в объект.
     */
    public AuthRequest() {}

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

    /**
     * Возвращает имя пользователя (username).
     * Отображается другим пользователям в чатах.
     *
     * @return имя пользователя
     */
    public String getUsername() { return username; }

    /**
     * Устанавливает имя пользователя.
     *
     * @param username имя пользователя
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Возвращает пароль пользователя.
     *
     * <p><b>Безопасность:</b> Пароль хэшируется перед сохранением в базу данных.</p>
     *
     * @return пароль пользователя (в открытом виде)
     */
    public String getPassword() { return password; }

    /**
     * Устанавливает пароль пользователя.
     *
     * @param password пароль пользователя
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Возвращает имя пользователя (first name).
     * Необязательное поле, используется для отображения в профиле.
     *
     * @return имя пользователя
     */
    public String getFirst_name() { return first_name; }

    /**
     * Устанавливает имя пользователя.
     *
     * @param first_name имя пользователя
     */
    public void setFirst_name(String first_name) { this.first_name = first_name; }

    /**
     * Возвращает фамилию пользователя.
     * Необязательное поле, используется для отображения в профиле.
     *
     * @return фамилия пользователя
     */
    public String getLast_name() { return last_name; }

    /**
     * Устанавливает фамилию пользователя.
     *
     * @param last_name фамилия пользователя
     */
    public void setLast_name(String last_name) { this.last_name = last_name; }
}