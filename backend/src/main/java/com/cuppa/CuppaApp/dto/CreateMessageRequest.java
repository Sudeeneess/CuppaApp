package com.cuppa.CuppaApp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для запроса на создание нового сообщения в чате.
 *
 * <p>Содержит минимально необходимые данные для отправки сообщения.
 * Используется как тело запроса для endpoint отправки сообщений.
 * Валидация гарантирует что обязательные поля заполнены корректно.
 *
 * <p><b>Endpoint:</b> POST /api/messages
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-14
 */
@Data
public class CreateMessageRequest {

    /**
     * Текст сообщения.
     * Обязательное поле, не может быть пустым.
     * Поддерживает сообщения любой длины.
     */
    @NotBlank(message = "Текст сообщения не может быть пустым")
    private String content;

    /**
     * Идентификатор чат-комнаты, в которую отправляется сообщение.
     * Обязательное поле.
     * Должен соответствовать существующей чат-комнате.
     */
    @NotNull(message = "Идентификатор чата обязателен")
    private Integer chatRoomId;

    /**
     * Тип сообщения.
     * Определяет способ обработки и отображения сообщения.
     * По умолчанию TEXT.
     * Возможные значения: TEXT, IMAGE, FILE, VOICE, VIDEO, SYSTEM.
     */
    private String messageType = "TEXT";
}