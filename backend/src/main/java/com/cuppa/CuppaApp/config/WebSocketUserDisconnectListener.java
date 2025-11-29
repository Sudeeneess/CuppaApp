package com.cuppa.CuppaApp.config;

import com.cuppa.CuppaApp.service.SecurityService;
import com.cuppa.CuppaApp.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * Обработчик событий отключения пользователей через WebSocket.
 * Отслеживает отключения и обновляет статус пользователя как оффлайн.
 *
 * @author Walerya Pleskova
 * @since 2025-11-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketUserDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {

    private final UserActivityService userActivityService;
    private final SecurityService securityService;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            try {
                Integer userId = securityService.getAuthenticatedUserId(principal);
                userActivityService.userDisconnected(userId);
                log.info("WebSocket Disconnect: User ID {} disconnected", userId);
            } catch (Exception e) {
                log.error("Error processing disconnect for principal: {}", principal.getName(), e);
            }
        }
    }
}