package com.cuppa.CuppaApp.dto;

import com.cuppa.CuppaApp.entity.ChatRoom;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) для представления данных чат-комнаты.
 *
 * <p>Используется для передачи информации о чат-комнатах между сервером и клиентом.
 * Содержит все необходимые поля для отображения списка чатов и детальной информации
 * о конкретном чате в пользовательском интерфейсе мессенджера.
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-14
 */
@Data
public class ChatRoomDto {

    /**
     * Уникальный идентификатор чат-комнаты.
     */
    private Integer id;

    /**
     * Название чат-комнаты.
     * Для приватных чатов может быть null.
     */
    private String name;

    /**
     * Тип чат-комнаты.
     * Определяет является ли чат приватным, групповым или публичным.
     */
    private ChatRoom.ChatRoomType type;

    /**
     * URL аватара чат-комнаты.
     * Может быть null если аватар не установлен.
     */
    private String avatarUrl;

    /**
     * Описание чат-комнаты.
     * Используется для групповых и публичных чатов.
     */
    private String description;

    /**
     * Текст последнего сообщения в чате.
     * Используется для превью в списке чатов.
     */
    private String lastMessageText;

    /**
     * Время отправки последнего сообщения.
     * Используется для сортировки чатов по активности.
     */
    private LocalDateTime lastMessageAt;

    /**
     * Дата и время создания чат-комнаты.
     */
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления чат-комнаты.
     */
    private LocalDateTime updatedAt;

    /**
     * Флаг активности чат-комнаты.
     * false означает что чат удален (мягкое удаление).
     */
    private Boolean isActive;

    /**
     * Максимальное количество участников в чате.
     * Для приватных чатов обычно равно 2.
     */
    private Integer maxParticipants;

    /**
     * Преобразует сущность ChatRoom в DTO объект.
     *
     * <p>Создает безопасное представление чат-комнаты для передачи клиенту.
     * Сохраняет все необходимые для отображения поля.
     *
     * @param chatRoom сущность чат-комнаты для преобразования
     * @return DTO объект с данными чат-комнаты
     */
    public static ChatRoomDto fromEntity(ChatRoom chatRoom) {
        ChatRoomDto dto = new ChatRoomDto();
        dto.setId(chatRoom.getId());
        dto.setName(chatRoom.getName());
        dto.setType(chatRoom.getType());
        dto.setAvatarUrl(chatRoom.getAvatarUrl());
        dto.setDescription(chatRoom.getDescription());
        dto.setLastMessageText(chatRoom.getLastMessageText());
        dto.setLastMessageAt(chatRoom.getLastMessageAt());
        dto.setCreatedAt(chatRoom.getCreatedAt());
        dto.setUpdatedAt(chatRoom.getUpdatedAt());
        dto.setIsActive(chatRoom.getIsActive());
        dto.setMaxParticipants(chatRoom.getMaxParticipants());
        return dto;
    }
}