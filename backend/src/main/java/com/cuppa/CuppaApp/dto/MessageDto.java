package com.cuppa.CuppaApp.dto;

import com.cuppa.CuppaApp.entity.Message;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) для представления данных сообщения в чате.
 *
 * <p>Содержит всю необходимую информацию о сообщении для отображения
 * в интерфейсе мессенджера, включая данные отправителя, временные метки
 * и статусы доставки/прочтения сообщения.
 *
 * <p><b>Использование:</b>
 * <ul>
 *   <li>Отображение истории сообщений в чате</li>
 *   <li>Показ статусов доставки и прочтения</li>
 *   <li>Отображение информации об отправителе</li>
 *   <li>Поддержка различных типов сообщений</li>
 * </ul>
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-14
 */
@Data
public class MessageDto {

    /**
     * Уникальный идентификатор сообщения.
     * Совпадает с первичным ключом в таблице messages.
     */
    private Integer id;

    /**
     * Текст сообщения или ссылка на медиа-контент.
     * Не может быть null или пустым.
     * Использует тип TEXT для хранения сообщений любой длины.
     */
    private String content;

    /**
     * Идентификатор чат-комнаты, в которой отправлено сообщение.
     * Внешний ключ к таблице chat_rooms.
     */
    private Integer chatRoomId;

    /**
     * Идентификатор отправителя сообщения.
     * Внешний ключ к таблице users.
     */
    private Integer senderId;

    /**
     * Имя отправителя для отображения в интерфейсе.
     * Используется для идентификации автора сообщения.
     */
    private String senderName;

    /**
     * URL аватара отправителя.
     * Может быть null если аватар не установлен.
     */
    private String senderAvatarUrl;

    /**
     * Тип сообщения.
     * Определяет тип содержимого и способ отображения.
     * Возможные значения: TEXT, IMAGE, FILE, VOICE, VIDEO, SYSTEM.
     */
    private String messageType;

    /**
     * Дата и время отправки сообщения.
     * Устанавливается автоматически при создании сообщения.
     * Используется для сортировки сообщений в чате.
     */
    private LocalDateTime sentAt;

    /**
     * Дата и время доставки сообщения всем участникам чата.
     * Может быть null если сообщение еще не доставлено.
     * Используется для показа статуса доставки.
     */
    private LocalDateTime deliveredAt;

    /**
     * Дата и время прочтения сообщения получателями.
     * Может быть null если сообщение еще не прочитано.
     * Используется для показа статуса прочтения.
     */
    private LocalDateTime readAt;

    /**
     * Флаг редактирования сообщения.
     * true - сообщение было отредактировано после отправки.
     * Используется для показа отметки "редактировано".
     */
    private Boolean isEdited;

    /**
     * Дата и время последнего редактирования сообщения.
     * Может быть null если сообщение никогда не редактировалось.
     * Используется для отслеживания истории изменений.
     */
    private LocalDateTime editedAt;

    /**
     * Преобразует сущность Message в DTO объект.
     *
     * <p>Создает безопасное представление сообщения для передачи клиенту.
     * Включает информацию об отправителе и все временные метки для
     * полноценного отображения в интерфейсе мессенджера.
     * Конвертирует Enum типы в строки для совместимости с фронтендом.
     *
     * @param message сущность сообщения для преобразования
     * @return DTO объект с данными сообщения
     * @throws NullPointerException если переданный message является null
     */
    public static MessageDto fromEntity(Message message) {
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setChatRoomId(message.getChatRoom().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getUsername());
        dto.setSenderAvatarUrl(message.getSender().getAvatarUrl());
        dto.setMessageType(message.getMessageType().name());
        dto.setSentAt(message.getSentAt());
        dto.setDeliveredAt(message.getDeliveredAt());
        dto.setReadAt(message.getReadAt());
        dto.setIsEdited(message.getIsEdited());
        dto.setEditedAt(message.getEditedAt());
        return dto;
    }
}