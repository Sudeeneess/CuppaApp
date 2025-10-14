package com.cuppa.CuppaApp.dto;

import com.cuppa.CuppaApp.entity.ChatRoom;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для запроса на создание новой чат-комнаты.
 *
 * <p>Содержит валидированные поля необходимые для создания чата.
 * Используется как тело запроса для endpoint создания чат-комнат.
 * Валидация гарантирует что обязательные поля заполнены корректно.
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-14
 */
@Data
public class CreateChatRequest {

    /**
     * Название новой чат-комнаты.
     * Обязательное поле для групповых и публичных чатов.
     * Для приватных чатов может быть пустым.
     */
    @NotBlank(message = "Название чата обязательно")
    private String name;

    /**
     * Тип создаваемой чат-комнаты.
     * Обязательное поле. Определяет основные параметры чата.
     */
    @NotNull(message = "Тип чата обязателен")
    private ChatRoom.ChatRoomType type;

    /**
     * Описание чат-комнаты.
     * Необязательное поле для дополнительной информации о чате.
     */
    private String description;

    /**
     * URL аватара чат-комнаты.
     * Необязательное поле для установки изображения чата.
     */
    private String avatarUrl;

    /**
     * Максимальное количество участников.
     * Если не указано, устанавливается значение по умолчанию в зависимости от типа чата.
     */
    private Integer maxParticipants;
}