package com.cuppa.CuppaApp.service;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс для отслеживания активности отдельного пользователя
 * Хранит историю действий и предоставляет методы для анализа
 *  @author Walerya Pleskova
 *  @version 1.0
 *  @since 28.10.2025
 */
@Data
class UserActivity {
    private List<UserAction> actions = new ArrayList<>();
    private LocalDateTime lastActivityTime = LocalDateTime.now();

    /**
     * Записывает действие пользователя
     */
    public void recordAction(AnomalyDetectionService.ActionType actionType, String resource, String details) {
        UserAction action = new UserAction(actionType, resource, details, LocalDateTime.now());
        actions.add(action);
        lastActivityTime = LocalDateTime.now();

        // Очищаем старые действия (старше 1 часа) для оптимизации памяти
        cleanupOldActions();
    }

    /**
     * Возвращает количество действий за последнюю минуту
     */
    public int getActionsLastMinute() {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        return (int) actions.stream()
                .filter(action -> action.getTimestamp().isAfter(oneMinuteAgo))
                .count();
    }

    /**
     * Возвращает количество неудачных попыток входа за последние 5 минут
     */
    public int getFailedLoginsLast5Minutes() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        return (int) actions.stream()
                .filter(action -> action.getTimestamp().isAfter(fiveMinutesAgo))
                .filter(action -> action.getActionType() == AnomalyDetectionService.ActionType.LOGIN_FAILED)
                .count();
    }

    /**
     * Возвращает количество отказов в доступе за последние 10 минут
     */
    public int getAccessDeniedLast10Minutes() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        return (int) actions.stream()
                .filter(action -> action.getTimestamp().isAfter(tenMinutesAgo))
                .filter(action -> action.getActionType() == AnomalyDetectionService.ActionType.ACCESS_DENIED)
                .count();
    }

    /**
     * Проверяет наличие подозрительных паттернов доступа
     */
    public boolean hasSuspiciousAccessPattern() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        // Получаем уникальные чаты, к которым был доступ за последние 10 минут
        Set<String> accessedChats = actions.stream()
                .filter(action -> action.getTimestamp().isAfter(tenMinutesAgo))
                .filter(action -> action.getActionType() == AnomalyDetectionService.ActionType.CHAT_ACCESS)
                .map(UserAction::getResource)
                .collect(Collectors.toSet());

        // Если пользователь обращался к слишком большому количеству разных чатов - подозрительно
        return accessedChats.size() > 8; // Более 8 разных чатов за 10 минут
    }

    /**
     * Очищает действия старше 1 часа
     */
    private void cleanupOldActions() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        actions.removeIf(action -> action.getTimestamp().isBefore(oneHourAgo));
    }

    /**
     * Внутренний класс для хранения информации о действии
     */
    @Data
    static class UserAction {
        private final AnomalyDetectionService.ActionType actionType;
        private final String resource;
        private final String details;
        private final LocalDateTime timestamp;

        public UserAction(AnomalyDetectionService.ActionType actionType, String resource,
                          String details, LocalDateTime timestamp) {
            this.actionType = actionType;
            this.resource = resource;
            this.details = details;
            this.timestamp = timestamp;
        }
    }
}