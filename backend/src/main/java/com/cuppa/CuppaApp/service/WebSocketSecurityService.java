package com.cuppa.CuppaApp.service;

import com.cuppa.CuppaApp.entity.User;
import com.cuppa.CuppaApp.repository.ChatParticipantRepository;
import com.cuppa.CuppaApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Сервис для обеспечения безопасности WebSocket соединений и проверки прав доступа к чатам.
 * Предоставляет методы для валидации доступа пользователей к чат-комнатам и проверки отправителей сообщений.
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 28.10.2025
 */
@Service
public class WebSocketSecurityService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    /**
     * Проверяет, что пользователь с указанным email состоит в указанном чате.
     * Выбрасывает AccessDeniedException, если пользователь не имеет доступа к чату.
     *
     * @param chatId идентификатор чат-комнаты для проверки доступа
     * @param userEmail email пользователя, для которого проверяется доступ
     * @throws AccessDeniedException если пользователь не состоит в указанном чате
     * @throws RuntimeException если произошла ошибка при проверке доступа
     */
    public void validateChatAccess(Integer chatId, String userEmail) {
        boolean hasAccess = chatParticipantRepository.existsByChatRoomIdAndUserEmail(chatId, userEmail);
        if (!hasAccess) {
            throw new AccessDeniedException("No access to chat: " + chatId);
        }
    }

    /**
     * Проверяет, что отправитель сообщения соответствует текущему аутентифицированному пользователю.
     * Предотвращает отправку сообщений от имени другого пользователя.
     *
     * @param senderId идентификатор отправителя, указанный в сообщении
     * @param userEmail email текущего аутентифицированного пользователя
     * @throws AccessDeniedException если идентификатор отправителя не соответствует текущему пользователю
     * @throws RuntimeException если пользователь с указанным email не найден
     */
    public void validateSender(Integer senderId, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!currentUser.getId().equals(senderId)) {
            throw new AccessDeniedException("Cannot send messages as another user");
        }
    }
}