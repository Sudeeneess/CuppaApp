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

    public ChatParticipantDto getChatParticipantById(Integer id) {
        ChatParticipant participant = chatParticipantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Участник чата не найден с ID: " + id));
        return ChatParticipantDto.fromEntity(participant);
    }

    public List<ChatParticipantDto> getChatParticipantsByChatId(Integer chatId) {
        return chatParticipantRepository.findByChatRoomId(chatId).stream()
                .map(ChatParticipantDto::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<ChatParticipantDto> getActiveChatParticipants(Integer chatId, Pageable pageable) {
        return chatParticipantRepository.findByChatRoomIdAndIsActiveTrue(chatId, pageable)
                .map(ChatParticipantDto::fromEntity);
    }

    public ChatParticipantDto createChatParticipant(ChatParticipantDto chatParticipantDto) {
        // Здесь должна быть логика создания участника чата
        // В реальном приложении нужно будет создать сущность из DTO
        throw new UnsupportedOperationException("Метод создания участника чата будет реализован позже");
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
}