package com.cuppa.CuppaApp.service;

import com.cuppa.CuppaApp.dto.ChatParticipantDto;
import com.cuppa.CuppaApp.entity.ChatParticipant;
import com.cuppa.CuppaApp.repository.ChatParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cuppa.CuppaApp.service.UserActivityService;
import com.cuppa.CuppaApp.repository.ChatRoomRepository;
import com.cuppa.CuppaApp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с участниками чатов
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatParticipantService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final UserActivityService userActivityService;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public ChatParticipantDto getChatParticipantById(Integer id) {
        ChatParticipant participant = chatParticipantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Участник чата не найден с ID: " + id));
        return ChatParticipantDto.fromEntity(participant);
    }

    public Page<ChatParticipantDto> getActiveChatParticipants(Integer chatId, Pageable pageable) {
        return chatParticipantRepository.findByChatRoomIdAndIsActiveTrue(chatId, pageable)
                .map(ChatParticipantDto::fromEntity);
    }

    public ChatParticipantDto createChatParticipant(ChatParticipantDto chatParticipantDto) {
        log.info("Создание участника чата для пользователя ID: {} в чате ID: {}",
                chatParticipantDto.getUserId(), chatParticipantDto.getChatRoomId());

        // Проверяем, не существует ли уже такой участник
        chatParticipantRepository.findByChatRoomIdAndUserId(
                chatParticipantDto.getChatRoomId(),
                chatParticipantDto.getUserId()
        ).ifPresent(participant -> {
            throw new RuntimeException("Пользователь уже является участником этого чата");
        });

        // Создаем нового участника чата
        ChatParticipant participant = new ChatParticipant();
        participant.setChatRoom(chatRoomRepository.getReferenceById(chatParticipantDto.getChatRoomId()));
        participant.setUser(userRepository.getReferenceById(chatParticipantDto.getUserId()));
        participant.setRole(ChatParticipant.ParticipantRole.valueOf(
                chatParticipantDto.getRole() != null ? chatParticipantDto.getRole() : "MEMBER"
        ));
        participant.setJoinedAt(LocalDateTime.now());
        participant.setIsActive(true);
        participant.setLastReadAt(LocalDateTime.now());

        ChatParticipant savedParticipant = chatParticipantRepository.save(participant);
        log.info("Участник чата создан с ID: {}", savedParticipant.getId());

        return ChatParticipantDto.fromEntity(savedParticipant);
    }

    public ChatParticipantDto updateParticipantRole(Integer id, String role) {
        ChatParticipant participant = chatParticipantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Участник чата не найден с ID: " + id));

        participant.setRole(ChatParticipant.ParticipantRole.valueOf(role));
        ChatParticipant updatedParticipant = chatParticipantRepository.save(participant);

        return ChatParticipantDto.fromEntity(updatedParticipant);
    }

    public ChatParticipantDto updateParticipantActiveStatus(Integer id, Boolean isActive) {
        ChatParticipant participant = chatParticipantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Участник чата не найден с ID: " + id));

        participant.setIsActive(isActive);
        ChatParticipant updatedParticipant = chatParticipantRepository.save(participant);

        return ChatParticipantDto.fromEntity(updatedParticipant);
    }

    public ChatParticipantDto updateLastReadTime(Integer id) {
        ChatParticipant participant = chatParticipantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Участник чата не найден с ID: " + id));

        participant.setLastReadAt(LocalDateTime.now());
        ChatParticipant updatedParticipant = chatParticipantRepository.save(participant);

        return ChatParticipantDto.fromEntity(updatedParticipant);
    }

    public void deleteChatParticipant(Integer id) {
        ChatParticipant participant = chatParticipantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Участник чата не найден с ID: " + id));

        chatParticipantRepository.delete(participant);
    }

    public ChatParticipantDto getChatParticipantByChatAndUser(Integer chatId, Integer userId) {
        ChatParticipant participant = chatParticipantRepository.findByChatRoomIdAndUserId(chatId, userId)
                .orElseThrow(() -> new RuntimeException(
                        "Участник чата не найден для пользователя ID: " + userId + " в чате ID: " + chatId));

        return ChatParticipantDto.fromEntity(participant);
    }

    /**
     * Возвращает Entity. Используется ChatRoomService для получения lastReadAt.
     */
    @Transactional(readOnly = true)
    public ChatParticipant findParticipantEntityByChatAndUser(Integer chatId, Integer userId) {
        // Убрал .orElseThrow, чтобы ChatRoomService мог обработать null
        return chatParticipantRepository.findByChatRoomIdAndUserId(chatId, userId).orElse(null);
    }

    /**
     * Вспомогательный метод для маппинга Entity в DTO с добавлением статуса.
     * Использует инжектированный UserActivityService.
     */
    private ChatParticipantDto mapEntityToDtoWithStatus(ChatParticipant participant) {
        ChatParticipantDto dto = ChatParticipantDto.fromEntity(participant);
        Integer userId = participant.getUser().getId();

        dto.setIsOnline(userActivityService.isUserOnline(userId));
        dto.setLastSeen(userActivityService.getLastSeen(userId));

        return dto;
    }

    public List<ChatParticipantDto> getChatParticipantsByChatId(Integer chatId) {
        return chatParticipantRepository.findByChatRoomId(chatId).stream()
                .map(this::mapEntityToDtoWithStatus)
                .collect(Collectors.toList());
    }


}