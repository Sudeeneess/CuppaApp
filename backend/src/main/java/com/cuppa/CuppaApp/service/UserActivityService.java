package com.cuppa.CuppaApp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для отслеживания статуса онлайн/оффлайн пользователей через WebSocket.
 * Хранит время последней активности пользователя в памяти (in-memory).
 *
 * @author Walerya Pleskova
 * @since 2025-11-29
 */
@Slf4j
@Service
public class UserActivityService {

    // Ключ: ID пользователя (Integer), Значение: Время последней активности (LocalDateTime)
    private final Map<Integer, LocalDateTime> activeUsers = new ConcurrentHashMap<>();

    // Порог неактивности (время, после которого пользователь считается оффлайн)
    private static final int INACTIVITY_THRESHOLD_MINUTES = 5;

    /**
     * Вызывается при подключении пользователя к WebSocket или при активности.
     */
    public void userConnected(Integer userId) {
        if (userId != null) {
            activeUsers.put(userId, LocalDateTime.now());
            log.info("User connected: ID {}", userId);
        }
    }

    /**
     * Вызывается при отключении пользователя.
     * Мы не удаляем запись, а просто логируем, чтобы getLastSeen оставался актуальным.
     */
    public void userDisconnected(Integer userId) {
        if (userId != null) {
            // Время активности не обновляем, оно останется как lastSeen
            log.info("User disconnected: ID {}", userId);
        }
    }

    /**
     * Проверяет, является ли пользователь активным (онлайн).
     */
    public boolean isUserOnline(Integer userId) {
        if (userId == null) {
            return false;
        }
        LocalDateTime lastSeen = activeUsers.get(userId);
        if (lastSeen == null) {
            return false;
        }
        // Считаем пользователя онлайн, если последняя активность была менее N минут назад
        return lastSeen.isAfter(LocalDateTime.now().minusMinutes(INACTIVITY_THRESHOLD_MINUTES));
    }

    /**
     * Получает время последнего посещения.
     */
    public LocalDateTime getLastSeen(Integer userId) {
        return activeUsers.get(userId);
    }
}