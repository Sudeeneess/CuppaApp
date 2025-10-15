package com.cuppa.CuppaApp.controllers;

import com.cuppa.CuppaApp.dto.ChatParticipantDto;
import com.cuppa.CuppaApp.entity.ChatParticipant;
import com.cuppa.CuppaApp.service.ChatParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления участниками чатов
 *
 * <p>Обеспечивает REST API для работы с участниками чат-комнат:
 * получение списка участников, управление ролями, отслеживание активности.
 *
 * @author Petr Panteev
 * @version 1.0
 * @since 15.10.2025
 */
@Slf4j
@RestController
@RequestMapping("/api/chat-participants")
@RequiredArgsConstructor
public class ChatParticipantController {

    private final ChatParticipantService chatParticipantService;

    /**
     * Получение участника чата по идентификатору
     *
     * @param id идентификатор участника чата
     * @return DTO участника чата
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChatParticipantDto> getChatParticipant(@PathVariable Integer id) {
        log.info("Получение участника чата с ID: {}", id);
        ChatParticipantDto participant = chatParticipantService.getChatParticipantById(id);
        return ResponseEntity.ok(participant);
    }

    /**
     * Получение всех участников конкретного чата
     *
     * @param chatId идентификатор чат-комнаты
     * @return список DTO участников чата
     */
    @GetMapping("/chat/{chatId}")
    public ResponseEntity<List<ChatParticipantDto>> getChatParticipantsByChatId(@PathVariable Integer chatId) {
        log.info("Получение участников чата с ID: {}", chatId);
        List<ChatParticipantDto> participants = chatParticipantService.getChatParticipantsByChatId(chatId);
        return ResponseEntity.ok(participants);
    }

    /**
     * Получение активных участников чата с пагинацией
     *
     * @param chatId идентификатор чат-комнаты
     * @param pageable параметры пагинации
     * @return страница с DTO участников чата
     */
    @GetMapping("/chat/{chatId}/active")
    public ResponseEntity<Page<ChatParticipantDto>> getActiveChatParticipants(
            @PathVariable Integer chatId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Получение активных участников чата с ID: {}, page: {}, size: {}",
                chatId, pageable.getPageNumber(), pageable.getPageSize());
        Page<ChatParticipantDto> participants = chatParticipantService.getActiveChatParticipants(chatId, pageable);
        return ResponseEntity.ok(participants);
    }

    /**
     * Создание нового участника чата
     *
     * @param chatParticipantDto DTO с данными участника
     * @return созданный DTO участника чата
     */
    @PostMapping
    public ResponseEntity<ChatParticipantDto> createChatParticipant(
            @Valid @RequestBody ChatParticipantDto chatParticipantDto) {
        log.info("Создание нового участника чата для пользователя ID: {} в чате ID: {}",
                chatParticipantDto.getUserId(), chatParticipantDto.getChatRoomId());
        ChatParticipantDto createdParticipant = chatParticipantService.createChatParticipant(chatParticipantDto);
        return ResponseEntity.ok(createdParticipant);
    }

    /**
     * Обновление роли участника чата
     *
     * @param id идентификатор участника чата
     * @param role новая роль участника
     * @return обновленный DTO участника чата
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<ChatParticipantDto> updateParticipantRole(
            @PathVariable Integer id,
            @RequestParam String role) {
        log.info("Обновление роли участника с ID: {} на роль: {}", id, role);
        ChatParticipantDto updatedParticipant = chatParticipantService.updateParticipantRole(id, role);
        return ResponseEntity.ok(updatedParticipant);
    }

    /**
     * Обновление статуса активности участника
     *
     * @param id идентификатор участника чата
     * @param isActive новый статус активности
     * @return обновленный DTO участника чата
     */
    @PutMapping("/{id}/active")
    public ResponseEntity<ChatParticipantDto> updateParticipantActiveStatus(
            @PathVariable Integer id,
            @RequestParam Boolean isActive) {
        log.info("Обновление статуса активности участника с ID: {} на: {}", id, isActive);
        ChatParticipantDto updatedParticipant = chatParticipantService.updateParticipantActiveStatus(id, isActive);
        return ResponseEntity.ok(updatedParticipant);
    }

    /**
     * Обновление времени последнего прочтения сообщений
     *
     * @param id идентификатор участника чата
     * @return обновленный DTO участника чата
     */
    @PutMapping("/{id}/last-read")
    public ResponseEntity<ChatParticipantDto> updateLastReadTime(@PathVariable Integer id) {
        log.info("Обновление времени последнего прочтения для участника с ID: {}", id);
        ChatParticipantDto updatedParticipant = chatParticipantService.updateLastReadTime(id);
        return ResponseEntity.ok(updatedParticipant);
    }

    /**
     * Удаление участника из чата
     *
     * @param id идентификатор участника чата
     * @return ответ с подтверждением удаления
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChatParticipant(@PathVariable Integer id) {
        log.info("Удаление участника чата с ID: {}", id);
        chatParticipantService.deleteChatParticipant(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Получение участника чата по идентификаторам пользователя и чата
     *
     * @param chatId идентификатор чат-комнаты
     * @param userId идентификатор пользователя
     * @return DTO участника чата
     */
    @GetMapping("/chat/{chatId}/user/{userId}")
    public ResponseEntity<ChatParticipantDto> getChatParticipantByChatAndUser(
            @PathVariable Integer chatId,
            @PathVariable Integer userId) {
        log.info("Получение участника чата для пользователя ID: {} в чате ID: {}", userId, chatId);
        ChatParticipantDto participant = chatParticipantService.getChatParticipantByChatAndUser(chatId, userId);
        return ResponseEntity.ok(participant);
    }
}