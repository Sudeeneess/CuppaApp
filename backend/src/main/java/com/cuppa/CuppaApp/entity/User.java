package com.cuppa.CuppaApp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность пользователя (User) для приложения Cuppa
 *
 * <p>Отображает таблицу "users" из базы данных в объектную модель Java.
 * Содержит основные поля профиля пользователя для системы мессенджера.
 *
 * @author Petr Panteev
 * @version 1.1
 * @since 09.10.2025
 */

@Table(name = "users")
@Entity
@Data
public class User {

    /**
     * Уникальный идентификатор пользователя
     *
     * <p>Совпадает с первичным ключом в таблице Users.
     * Является автоинкрементным полем в базе данных.
     */
    @Id // Указывает, что поле является первичным ключом
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Сопоставляет с колонкой "id" в таблице
    private Integer id;

    /**
     * Уникальное имя пользователя для входа в систему
     */
    @Column(name = "username", nullable = false, length = 255)
    private String username;

    /**
     * Электронная почта пользователя
     */
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /**
     * Хеш пароля пользователя
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * Имя пользователя
     */
    @Column(name = "first_name", length = 255)
    private String firstName;

    /**
     * Фамилия пользователя
     */
    @Column(name = "last_name", length = 255)
    private String lastName;

    /**
     * URL аватара пользователя
     */
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    /**
     * Номер телефона пользователя
     */
    @Column(name = "phone", length = 255)
    private String phone;

    /**
     * Статус онлайн пользователя
     */
    @Column(name = "is_online")
    private Boolean isOnline = false;

    /**
     * Время последней активности пользователя
     */
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    /**
     * Дата и время создания учетной записи
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления данных пользователя
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}