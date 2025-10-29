package com.cuppa.CuppaApp.dto;

import lombok.Data;

/**
 * Data Transfer Object для события набора текста (typing indicator).
 * Используется для уведомления других участников чата о действиях пользователя.
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 29.10.2025
 */


@Data
public class TypingEventDto {
    /**
     * Уникальный идентификатор пользователя, который печатает
     */
    private Integer userId;

    /**
     * Отображаемое имя пользователя
     */
    private String userName;

    /**
     * Флаг состояния набора текста:
     * {@code true} - пользователь начал печатать,
     * {@code false} - пользователь закончил печатать
     */
    private Boolean isTyping;
}
