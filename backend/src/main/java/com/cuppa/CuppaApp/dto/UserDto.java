package com.cuppa.CuppaApp.dto;

import com.cuppa.CuppaApp.entity.User;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) для безопасного представления данных пользователя.
 *
 * <p>Используется для передачи данных между сервером и клиентом без раскрытия
 * чувствительной информации, такой как хэши паролей. Содержит только те поля,
 * которые необходимы для отображения в пользовательском интерфейсе.
 *
 * <p><b>Особенности:</b>
 * <ul>
 *   <li>Не содержит поля passwordHash для обеспечения безопасности</li>
 *   <li>Использует camelCase нотацию для совместимости с фронтендом</li>
 *   <li>Поддерживает все основные поля профиля пользователя</li>
 * </ul>
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-14
 */
@Data
public class UserDto {

    /**
     * Уникальный идентификатор пользователя в системе.
     * Совпадает с первичным ключом в таблице users.
     */
    private Integer id;

    /**
     * Уникальное имя пользователя для отображения в интерфейсе.
     * Не может быть null.
     */
    private String username;

    /**
     * Электронная почта пользователя.
     * Используется для аутентификации и уведомлений.
     */
    private String email;

    /**
     * Имя пользователя (необязательное поле).
     * Может быть null если пользователь не указал имя.
     */
    private String firstName;

    /**
     * Фамилия пользователя (необязательное поле).
     * Может быть null если пользователь не указал фамилию.
     */
    private String lastName;

    /**
     * URL аватара пользователя.
     * Может быть null если аватар не установлен.
     */
    private String avatarUrl;

    /**
     * Номер телефона пользователя (необязательное поле).
     * Может быть null если телефон не указан.
     */
    private String phone;

    /**
     * Статус онлайн пользователя.
     * true - пользователь онлайн, false - оффлайн.
     */
    private Boolean isOnline;

    /**
     * Время последней активности пользователя.
     * Обновляется при каждом действии пользователя в системе.
     */
    private LocalDateTime lastSeen;

    /**
     * Дата и время создания учетной записи.
     * Устанавливается автоматически при регистрации.
     */
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления данных пользователя.
     * Обновляется при изменении любой информации о пользователе.
     */
    private LocalDateTime updatedAt;

    /**
     * Преобразует сущность User в DTO объект.
     *
     * <p>Копирует все безопасные поля из Entity в DTO, исключая чувствительные
     * данные такие как хэш пароля. Гарантирует что пароль никогда не будет
     * передан клиенту.
     *
     * @param user сущность пользователя для преобразования
     * @return DTO объект с безопасными данными пользователя
     * @throws NullPointerException если переданный user является null
     */
    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setPhone(user.getPhone());
        dto.setIsOnline(user.getIsOnline());
        dto.setLastSeen(user.getLastSeen());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}