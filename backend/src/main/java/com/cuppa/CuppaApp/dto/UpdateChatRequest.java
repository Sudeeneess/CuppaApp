package com.cuppa.CuppaApp.dto;

import lombok.Data;

/**
 * DTO для запроса на обновление данных чат-комнаты.
 *
 * <p>Содержит поля которые можно изменять при обновлении информации о чате.
 * Все поля являются необязательными - обновляются только переданные значения.
 * Это позволяет выполнять частичное обновление данных чат-комнаты.
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-14
 */
@Data
public class UpdateChatRequest {

    /**
     * Новое название чат-комнаты.
     * Если null, название не изменяется.
     */
    private String name;

    /**
     * Новый URL аватара чат-комнаты.
     * Если null, аватар не изменяется.
     */
    private String avatarUrl;

    /**
     * Новое описание чат-комнаты.
     * Если null, описание не изменяется.
     */
    private String description;

    /**
     * Новое максимальное количество участников.
     * Если null, ограничение не изменяется.
     */
    private Integer maxParticipants;
}