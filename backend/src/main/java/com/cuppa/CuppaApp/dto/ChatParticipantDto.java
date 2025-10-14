package com.cuppa.CuppaApp.dto;

import com.cuppa.CuppaApp.entity.ChatParticipant;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) для представления данных участника чата.
 *
 * <p>Содержит информацию о пользователе как участнике чат-комнаты,
 * включая время присоединения к чату, роль участника и статус активности.
 * Обеспечивает безопасную передачу данных между сервером и клиентом.
 *
 * <p><b>Использование:</b>
 * <ul>
 *   <li>Отображение списка участников чата</li>
 *   <li>Управление правами доступа в чате</li>
 *   <li>Отслеживание активности участников</li>
 * </ul>
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-14
 */
@Data
public class ChatParticipantDto {

    /**
     * Уникальный идентификатор записи участника чата.
     * Совпадает с первичным ключом в таблице chat_participants.
     */
    private Integer id;

    /**
     * Идентификатор чат-комнаты, в которой состоит участник.
     * Внешний ключ к таблице chat_rooms.
     */
    private Integer chatRoomId;

    /**
     * Идентификатор пользователя-участника.
     * Внешний ключ к таблице users.
     */
    private Integer userId;

    /**
     * Имя пользователя для отображения в интерфейсе.
     * Используется для идентификации участника в списке.
     */
    private String userName;

    /**
     * URL аватара пользователя.
     * Может быть null если аватар не установлен.
     */
    private String userAvatarUrl;

    /**
     * Роль участника в чате.
     * Определяет права доступа и возможности пользователя.
     * Возможные значения: MEMBER, ADMIN, MODERATOR, OWNER.
     */
    private String role;

    /**
     * Дата и время присоединения пользователя к чату.
     * Устанавливается автоматически при добавлении участника.
     */
    private LocalDateTime joinedAt;

    /**
     * Флаг активности участника в чате.
     * true - участник активен, false - покинул чат.
     * Неактивные участники сохраняются для истории.
     */
    private Boolean isActive;

    /**
     * Дата и время последнего прочтения сообщений в чате.
     * Используется для отслеживания непрочитанных сообщений.
     * Может быть null если пользователь еще не читал сообщения.
     */
    private LocalDateTime lastReadAt;

    /**
     * Преобразует сущность ChatParticipant в DTO объект.
     *
     * <p>Создает безопасное представление участника чата с основной информацией
     * о пользователе для удобства отображения в интерфейсе мессенджера.
     * Конвертирует Enum типы в строки для совместимости с фронтендом.
     *
     * @param participant сущность участника чата для преобразования
     * @return DTO объект с данными участника чата
     * @throws NullPointerException если переданный participant является null
     */
    public static ChatParticipantDto fromEntity(ChatParticipant participant) {
        ChatParticipantDto dto = new ChatParticipantDto();
        dto.setId(participant.getId());
        dto.setChatRoomId(participant.getChatRoom().getId());
        dto.setUserId(participant.getUser().getId());
        dto.setUserName(participant.getUser().getUsername());
        dto.setUserAvatarUrl(participant.getUser().getAvatarUrl());
        dto.setRole(participant.getRole().name());
        dto.setJoinedAt(participant.getJoinedAt());
        dto.setIsActive(participant.getIsActive());
        dto.setLastReadAt(participant.getLastReadAt());
        return dto;
    }
}