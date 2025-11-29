package com.cuppa.CuppaApp.dto;

import com.cuppa.CuppaApp.entity.ChatRoom;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object (DTO) для представления данных чат-комнаты.
 *
 * <p>Используется для передачи информации о чат-комнатах между сервером и клиентом.
 * Содержит все необходимые поля для отображения списка чатов и детальной информации
 * о конкретном чате в пользовательском интерфейсе мессенджера.
 *
 * @author Walerya Pleskova
 * @since 2025-11-29
 */
@Data
public class ChatRoomDto {
    private Integer id;
    private String name;
    private ChatRoom.ChatRoomType type;
    private String avatarUrl;
    private String description;
    private String lastMessageText;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private Integer maxParticipants;
    private Integer unreadCount;
    private List<ChatParticipantDto> participants;
    private Boolean isOnline;
    private LocalDateTime lastSeen;

    /**
     * Преобразует сущность ChatRoom в DTO объект.
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