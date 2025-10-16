package com.cuppa.CuppaApp.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO для передачи сообщений через WebSocket
 *
 * <p>Используется для реального времени обмена сообщениями между клиентами
 * через WebSocket соединение.
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-16
 */
@Data
public class WebSocketMessageDto {

    /**
     * Тип сообщения для определения обработки на клиенте
     *
     * <p>Возможные значения:
     * <ul>
     *   <li>CHAT_MESSAGE - обычное сообщение чата</li>
     *   <li>USER_JOINED - пользователь присоединился к чату</li>
     *   <li>USER_LEFT - пользователь покинул чат</li>
     *   <li>TYPING - пользователь печатает</li>
     *   <li>MESSAGE_READ - сообщение прочитано</li>
     * </ul>
     */
    private MessageType type;

    /**
     * Идентификатор чат-комнаты
     */
    private Integer chatRoomId;

    /**
     * Идентификатор отправителя
     */
    private Integer senderId;

    /**
     * Имя отправителя для отображения
     */
    private String senderName;

    /**
     * Содержимое сообщения
     */
    private String content;

    /**
     * Временная метка отправки
     */
    private LocalDateTime timestamp;

    /**
     * Дополнительные данные в зависимости от типа сообщения
     */
    private Object payload;

    /**
     * Типы WebSocket сообщений
     */
    public enum MessageType {
        CHAT_MESSAGE,
        USER_JOINED,
        USER_LEFT,
        TYPING,
        MESSAGE_READ
    }
}