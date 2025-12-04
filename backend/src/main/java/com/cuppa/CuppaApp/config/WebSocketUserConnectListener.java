package com.cuppa.CuppaApp.config;

import com.cuppa.CuppaApp.service.SecurityService;
import com.cuppa.CuppaApp.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.security.Principal;

/**
 * Обработчик событий подключения пользователей через WebSocket.
 * Отслеживает подключения и обновляет статус пользователя как онлайн.
 *
 * @author Walerya Pleskova
 * @since 2025-11-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketUserConnectListener implements ApplicationListener<SessionConnectEvent> {

    private final UserActivityService userActivityService;
    private final SecurityService securityService;

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            try {
                Integer userId = securityService.getAuthenticatedUserId(principal);
                userActivityService.userConnected(userId);
                log.info("WebSocket Connect: User ID {} connected", userId);
            } catch (Exception e) {
                log.error("Error processing connect for principal: {}", principal.getName(), e);
            }
        }
    }
}