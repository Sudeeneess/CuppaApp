package com.cuppa.CuppaApp.dto;

import lombok.Data;
/**
 * Data Transfer Object для события прочтения сообщений.
 * Используется для отслеживания статуса прочтения сообщений в чате.
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 29.10.2025
 */

@Data
public class MessageReadEventDto {
    /**
     * Уникальный идентификатор пользователя, который прочитал сообщения
     */
    private Integer userId;

    /**
     * Отображаемое имя пользователя
     */
    private String userName;

    /**
     * Идентификатор последнего прочитанного сообщения в чате.
     * Все сообщения с ID меньше или равным этому считаются прочитанными.
     */
    private Integer lastReadMessageId;
}
