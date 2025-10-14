package com.cuppa.CuppaApp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для запроса на добавление участника в чат-комнату.
 *
 * <p>Содержит данные необходимые для добавления пользователя в существующий чат.
 * Используется для endpoint управления участниками чатов.
 * Валидация гарантирует что обязательные поля заполнены.
 *
 * <p><b>Endpoint:</b> POST /api/chats/{chatId}/participants
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-14
 */
@Data
public class AddParticipantRequest {

    /**
     * Идентификатор пользователя, которого добавляют в чат.
     * Обязательное поле.
     * Должен соответствовать существующему пользователю.
     */
    @NotNull(message = "Идентификатор пользователя обязателен")
    private Integer userId;

    /**
     * Роль участника в чате.
     * Определяет права доступа и возможности пользователя.
     * По умолчанию MEMBER.
     * Возможные значения: MEMBER, ADMIN, MODERATOR, OWNER.
     */
    private String role = "MEMBER";
}