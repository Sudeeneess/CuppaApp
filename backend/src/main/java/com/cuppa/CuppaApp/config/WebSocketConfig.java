package com.cuppa.CuppaApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация WebSocket для реального времени в мессенджере
 *
 * <p>Настраивает STOMP поверх WebSocket для обмена сообщениями в реальном времени
 * между клиентами и сервером. Включает настройки безопасности CORS.
 *
 * @author Pleskova Walerya
 * @version 2.1
 * @since 2025-10-28
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.security.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    /**
     * Настраивает брокер сообщений для WebSocket
     *
     * <p>Конфигурирует префиксы для:
     * <ul>
     *   <li>Брокера (topic, queue) - куда сервер отправляет сообщения</li>
     *   <li>Приложения (app) - куда клиенты отправляют сообщения</li>
     * </ul>
     *
     * @param config реестр для настройки брокера сообщений
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Включаем простой брокер в памяти для темов и очередей
        config.enableSimpleBroker("/topic", "/queue");

        // Префикс для сообщений, которые направляются в методы с @MessageMapping
        config.setApplicationDestinationPrefixes("/app");

        // Префикс для приватных сообщений конкретным пользователям
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Регистрирует WebSocket endpoint для подключения клиентов
     *
     * <p>Клиенты подключаются к этому endpoint для установки WebSocket соединения.
     * SockJS обеспечивает fallback для браузеров без поддержки WebSocket.
     * Использует безопасные настройки CORS.
     *
     * @param registry реестр для регистрации WebSocket endpoints
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //  НАСТРОЙКИ CORS ДЛЯ WEB SOCKET
        String[] allowedOriginPatterns = getAllAllowedOriginPatterns();

        // Регистрируем endpoint для WebSocket с SockJS fallback
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns(allowedOriginPatterns) //Только разрешенные домены
                .withSockJS(); // Fallback для старых браузеров
    }

    /**
     * Преобразует строку разрешенных источников в массив паттернов
     *
     * @return массив разрешенных origin паттернов
     */
    private String[] getAllAllowedOriginPatterns() {
        List<String> origins = Arrays.asList(allowedOrigins.split(","));

        // Добавляем localhost паттерны для разработки

        return new String[] {
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:8080",
                "http://localhost:8081",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173",
                "https://cuppaapp.onrender.com"
        };
    }
}