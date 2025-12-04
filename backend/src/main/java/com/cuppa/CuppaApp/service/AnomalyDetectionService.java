package com.cuppa.CuppaApp.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для обнаружения подозрительной и аномальной активности пользователей
 * Мониторит частоту запросов, подозрительные действия и потенциальные атаки
 *
 * @author Walerya Pleskova
 * @since 2025-11-29
 */
@Slf4j
@Service
public class AnomalyDetectionService {

    private final Map<String, UserActivity> userActivities = new ConcurrentHashMap<>();
    private final Map<String, List<SecurityEvent>> securityEvents = new ConcurrentHashMap<>();

    // Конфигурация детектора аномалий
    private static final int SUSPICIOUS_ACTIONS_THRESHOLD = 10; // Подозрительных действий в минуту
    private static final int FAILED_LOGIN_THRESHOLD = 5; // Неудачных попыток входа за 5 минут
    private static final int ACCESS_DENIED_THRESHOLD = 8; // Отказов в доступе за 10 минут

    /**
     * Записывает действие пользователя для анализа аномалий
     *
     * @param userEmail email пользователя
     * @param actionType тип действия (LOGIN, SEND_MESSAGE, ACCESS_DENIED, etc.)
     * @param resource ресурс к которому был доступ
     * @param details дополнительные детали действия
     */
    public void recordUserAction(String userEmail, ActionType actionType, String resource, String details) {
        UserActivity activity = userActivities.getOrDefault(userEmail, new UserActivity());
        activity.recordAction(actionType, resource, details);
        userActivities.put(userEmail, activity);

        // Проверяем аномалии после записи действия
        checkForAnomalies(userEmail, activity);
    }

    /**
     * Проверяет наличие аномальной активности для пользователя
     *
     * @param userEmail email пользователя
     * @param activity объект активности пользователя
     */
    private void checkForAnomalies(String userEmail, UserActivity activity) {
        LocalDateTime now = LocalDateTime.now();
        boolean hasAnomalies = false;
        List<String> anomalies = new ArrayList<>();

        // 1. Проверка слишком частых действий
        if (activity.getActionsLastMinute() > SUSPICIOUS_ACTIONS_THRESHOLD) {
            anomalies.add("Слишком высокая частота действий: " + activity.getActionsLastMinute() + " в минуту");
            hasAnomalies = true;
        }

        // 2. Проверка множественных неудачных попыток входа
        if (activity.getFailedLoginsLast5Minutes() > FAILED_LOGIN_THRESHOLD) {
            anomalies.add("Множественные неудачные попытки входа: " +
                    activity.getFailedLoginsLast5Minutes() + " за 5 минут");
            hasAnomalies = true;
        }

        // 3. Проверка множественных отказов в доступе
        if (activity.getAccessDeniedLast10Minutes() > ACCESS_DENIED_THRESHOLD) {
            anomalies.add("Множественные отказы в доступе: " +
                    activity.getAccessDeniedLast10Minutes() + " за 10 минут");
            hasAnomalies = true;
        }

        // 4. Проверка подозрительных паттернов доступа
        if (activity.hasSuspiciousAccessPattern()) {
            anomalies.add("Обнаружен подозрительный паттерн доступа к разным чатам");
            hasAnomalies = true;
        }

        // Логируем аномалии если они обнаружены
        if (hasAnomalies) {
            logSecurityEvent(SecurityEventType.ANOMALY_DETECTED, userEmail,
                    String.join("; ", anomalies), now);

            // Можно добавить уведомление администратору или временную блокировку
            notifyAdministrator(userEmail, anomalies);
        }
    }

    /**
     * Уведомляет администратора о подозрительной активности
     */
    private void notifyAdministrator(String userEmail, List<String> anomalies) {
        // В реальной системе здесь можно отправить email, сообщение в чат и т.д.
        log.warn("ANOMALY ALERT - User: {}, Anomalies: {}", userEmail, anomalies);

        // Пример интеграции с системой уведомлений
        String alertMessage = String.format(
                "Обнаружена подозрительная активность для пользователя %s:\n%s",
                userEmail, String.join("\n", anomalies)
        );

        // Здесь можно вызвать сервис уведомлений
        // notificationService.sendSecurityAlert(alertMessage);
    }

    /**
     * Регистрирует событие безопасности
     */
    public void logSecurityEvent(SecurityEventType eventType, String userEmail, String details, LocalDateTime timestamp) {
        SecurityEvent event = new SecurityEvent(eventType, userEmail, details, timestamp);
        securityEvents.computeIfAbsent(userEmail, k -> new ArrayList<>()).add(event);

        log.warn("SECURITY_EVENT - Type: {}, User: {}, Details: {}, Time: {}",
                eventType, userEmail, details, timestamp);
    }

    /**
     * Возвращает статистику активности для пользователя
     */
    public UserActivityStats getUserStats(String userEmail) {
        UserActivity activity = userActivities.get(userEmail);
        if (activity == null) {
            return new UserActivityStats(userEmail, 0, 0, 0);
        }

        return new UserActivityStats(
                userEmail,
                activity.getActionsLastMinute(),
                activity.getFailedLoginsLast5Minutes(),
                activity.getAccessDeniedLast10Minutes()
        );
    }

    /**
     * Возвращает все события безопасности для пользователя
     */
    public List<SecurityEvent> getSecurityEvents(String userEmail) {
        return securityEvents.getOrDefault(userEmail, new ArrayList<>());
    }

    /**
     * Очищает старые данные для оптимизации памяти
     */
    public void cleanupOldData() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);

        userActivities.entrySet().removeIf(entry ->
                entry.getValue().getLastActivityTime().isBefore(cutoffTime)
        );

        securityEvents.entrySet().removeIf(entry ->
                entry.getValue().removeIf(event -> event.getTimestamp().isBefore(cutoffTime))
        );
    }

    /**
     * Типы действий для отслеживания
     */
    public enum ActionType {
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        SEND_MESSAGE,
        READ_MESSAGE,
        ACCESS_DENIED,
        CHAT_ACCESS,
        SEARCH_MESSAGES,
        DELETE_MESSAGE,
        UPDATE_MESSAGE
    }

    /**
     * Типы событий безопасности
     */
    public enum SecurityEventType {
        ANOMALY_DETECTED,
        MULTIPLE_FAILED_LOGINS,
        RATE_LIMIT_EXCEEDED,
        SUSPICIOUS_ACCESS_PATTERN,
        XSS_ATTEMPT,
        UNAUTHORIZED_ACCESS_ATTEMPT
    }

    /**
     * Класс для хранения события безопасности
     */
    @Data
    public static class SecurityEvent {
        private final SecurityEventType eventType;
        private final String userEmail;
        private final String details;
        private final LocalDateTime timestamp;

        public SecurityEvent(SecurityEventType eventType, String userEmail, String details, LocalDateTime timestamp) {
            this.eventType = eventType;
            this.userEmail = userEmail;
            this.details = details;
            this.timestamp = timestamp;
        }
    }

    /**
     * Статистика активности пользователя
     */
    @Data
    public static class UserActivityStats {
        private final String userEmail;
        private final int actionsLastMinute;
        private final int failedLoginsLast5Minutes;
        private final int accessDeniedLast10Minutes;

        public UserActivityStats(String userEmail, int actionsLastMinute,
                                 int failedLoginsLast5Minutes, int accessDeniedLast10Minutes) {
            this.userEmail = userEmail;
            this.actionsLastMinute = actionsLastMinute;
            this.failedLoginsLast5Minutes = failedLoginsLast5Minutes;
            this.accessDeniedLast10Minutes = accessDeniedLast10Minutes;
        }
    }
}