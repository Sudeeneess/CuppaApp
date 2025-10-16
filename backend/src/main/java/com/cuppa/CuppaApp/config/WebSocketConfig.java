package com.cuppa.CuppaApp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Конфигурация WebSocket для реального времени в мессенджере
 *
 * <p>Настраивает STOMP поверх WebSocket для обмена сообщениями в реальном времени
 * между клиентами и сервером.
 *
 * @author Pleskova Walerya
 * @version 1.0
 * @since 2025-10-16
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

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
     *
     * @param registry реестр для регистрации WebSocket endpoints
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Регистрируем endpoint для WebSocket с SockJS fallback
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // Разрешаем все origin для разработки
                .withSockJS(); // Fallback для старых браузеров
    }
}