package com.cuppa.CuppaApp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для ограничения частоты запросов (Rate Limiting)
 * Защищает API от DDoS атак и злоупотреблений
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 28.10.2025
 */
@Slf4j
@Service
public class RateLimitService {

    private final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 100; // Лимит на пользователя в минуту
    private static final int MAX_REQUESTS_PER_HOUR = 1000; // Лимит на пользователя в час

    /**
     * Проверяет не превышен ли лимит запросов для пользователя
     *
     * @param userIdentifier идентификатор пользователя (email или IP)
     * @return true если запрос разрешен, false если лимит превышен
     */
    public boolean isAllowed(String userIdentifier) {
        RequestInfo info = requestCounts.getOrDefault(userIdentifier, new RequestInfo());
        LocalDateTime now = LocalDateTime.now();

        // Сбрасываем счетчики если прошла минута/час
        if (info.lastMinuteReset == null ||
                info.lastMinuteReset.isBefore(now.minusMinutes(1))) {
            info.minuteCount = 0;
            info.lastMinuteReset = now;
        }

        if (info.lastHourReset == null ||
                info.lastHourReset.isBefore(now.minusHours(1))) {
            info.hourCount = 0;
            info.lastHourReset = now;
        }

        // Проверяем лимиты
        if (info.minuteCount >= MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded per minute for user: {}", userIdentifier);
            return false;
        }

        if (info.hourCount >= MAX_REQUESTS_PER_HOUR) {
            log.warn("Rate limit exceeded per hour for user: {}", userIdentifier);
            return false;
        }

        // Увеличиваем счетчики
        info.minuteCount++;
        info.hourCount++;
        requestCounts.put(userIdentifier, info);

        return true;
    }

    private static class RequestInfo {
        int minuteCount = 0;
        int hourCount = 0;
        LocalDateTime lastMinuteReset;
        LocalDateTime lastHourReset;
    }
}