package com.cuppa.CuppaApp.service;

import com.cuppa.CuppaApp.entity.User;
import com.cuppa.CuppaApp.repository.ChatParticipantRepository;
import com.cuppa.CuppaApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Сервис для проверки прав доступа в REST API
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 28.10.2025
 */
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;

    /**
     * Проверяет, имеет ли текущий пользователь доступ к указанному чату
     *
     * @param chatId идентификатор чат-комнаты
     * @return true если пользователь имеет доступ к чату, иначе false
     */
    public boolean hasAccessToChat(Integer chatId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return chatParticipantRepository.existsByChatRoomIdAndUserEmail(chatId, email);
    }

    /**
     * Возвращает текущего аутентифицированного пользователя
     *
     * @return сущность текущего пользователя
     * @throws UsernameNotFoundException если пользователь не найден
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    /**
     * Проверяет, является ли указанный пользователь текущим аутентифицированным пользователем
     *
     * @param userId идентификатор пользователя для проверки
     * @return true если userId соответствует текущему пользователю, иначе false
     */
    public boolean isCurrentUser(Integer userId) {
        User currentUser = getCurrentUser();
        return currentUser.getId().equals(userId);
    }
}