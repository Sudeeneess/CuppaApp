package com.cuppa.CuppaApp.controllers;

import com.cuppa.CuppaApp.dto.MessageDto;
import com.cuppa.CuppaApp.service.MessageService;
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
 * Контроллер для управления сообщениями в чат-комнатах
 *
 * <p>Обеспечивает REST API для работы с сообщениями системы мессенджера:
 * отправка сообщений, получение истории сообщений, управление статусами
 * доставки и прочтения, поиск и фильтрация сообщений.
 *
 * <p>Поддерживает различные типы сообщений (текст, изображения, файлы)
 * и обеспечивает надежную доставку сообщений с отслеживанием статусов.
 *
 * @author Petr Panteev
 * @version 1.0
 * @since 09.10.2025
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * Получение сообщения по идентификатору
     *
     * @param id идентификатор сообщения
     * @return DTO сообщения
     */
    @GetMapping("/{id}")
    public ResponseEntity<MessageDto> getMessage(@PathVariable Integer id) {
        log.info("Получение сообщения с ID: {}", id);
        MessageDto message = messageService.getMessageById(id);
        return ResponseEntity.ok(message);
    }

    /**
     * Получение истории сообщений чата с пагинацией
     *
     * <p>Возвращает страницу сообщений для указанной чат-комнаты,
     * отсортированных по времени отправки в порядке убывания
     * (от новых к старым).
     *
     * @param chatId идентификатор чат-комнаты
     * @param pageable параметры пагинации
     * @return страница с DTO сообщений
     */
    @GetMapping("/chat/{chatId}")
    public ResponseEntity<Page<MessageDto>> getChatMessages(
            @PathVariable Integer chatId,
            @PageableDefault(size = 50, sort = "sentAt") Pageable pageable) {
        log.info("Получение сообщений чата с ID: {}, page: {}, size: {}",
                chatId, pageable.getPageNumber(), pageable.getPageSize());
        Page<MessageDto> messages = messageService.getMessagesByChatId(chatId, pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * Отправка нового сообщения в чат
     *
     * <p>Создает новое сообщение в указанной чат-комнате от имени
     * указанного пользователя. Время отправки устанавливается автоматически.
     *
     * @param messageDto DTO с данными сообщения
     * @return созданное DTO сообщения
     */
    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(@Valid @RequestBody MessageDto messageDto) {
        log.info("Отправка нового сообщения в чат ID: {} от пользователя ID: {}",
                messageDto.getChatRoomId(), messageDto.getSenderId());
        MessageDto createdMessage = messageService.createMessage(messageDto);
        return ResponseEntity.ok(createdMessage);
    }

    /**
     * Редактирование существующего сообщения
     *
     * <p>Обновляет содержимое сообщения и устанавливает флаг редактирования.
     * Время последнего редактирования обновляется автоматически.
     *
     * @param id идентификатор сообщения
     * @param messageDto DTO с обновленными данными сообщения
     * @return обновленное DTO сообщения
     */
    @PutMapping("/{id}")
    public ResponseEntity<MessageDto> updateMessage(
            @PathVariable Integer id,
            @Valid @RequestBody MessageDto messageDto) {
        log.info("Редактирование сообщения с ID: {}", id);
        MessageDto updatedMessage = messageService.updateMessage(id, messageDto);
        return ResponseEntity.ok(updatedMessage);
    }

    /**
     * Удаление сообщения
     *
     * <p>Удаляет сообщение из системы. В реальном приложении может
     * использоваться мягкое удаление (soft delete) для сохранения
     * истории переписки.
     *
     * @param id идентификатор сообщения
     * @return ответ с подтверждением удаления
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Integer id) {
        log.info("Удаление сообщения с ID: {}", id);
        messageService.deleteMessage(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Поиск сообщений по содержимому в указанном чате
     *
     * <p>Выполняет полнотекстовый поиск сообщений, содержащих
     * указанный текст в содержимом. Поиск не зависит от регистра.
     *
     * @param chatId идентификатор чат-комнаты
     * @param query текст для поиска
     * @param pageable параметры пагинации
     * @return страница с найденными сообщениями
     */
    @GetMapping("/chat/{chatId}/search")
    public ResponseEntity<Page<MessageDto>> searchMessages(
            @PathVariable Integer chatId,
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Поиск сообщений в чате ID: {} по запросу: '{}'", chatId, query);
        Page<MessageDto> messages = messageService.searchMessages(chatId, query, pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * Получение сообщений определенного типа в чате
     *
     * <p>Возвращает все сообщения указанного типа (TEXT, IMAGE, FILE и т.д.)
     * в рамках конкретной чат-комнаты.
     *
     * @param chatId идентификатор чат-комнаты
     * @param messageType тип сообщения
     * @return список сообщений указанного типа
     */
    @GetMapping("/chat/{chatId}/type/{messageType}")
    public ResponseEntity<List<MessageDto>> getMessagesByType(
            @PathVariable Integer chatId,
            @PathVariable String messageType) {
        log.info("Получение сообщений типа '{}' в чате ID: {}", messageType, chatId);
        List<MessageDto> messages = messageService.getMessagesByType(chatId, messageType);
        return ResponseEntity.ok(messages);
    }

    /**
     * Получение последнего сообщения в чате
     *
     * <p>Возвращает самое последнее сообщение в указанной чат-комнате
     * для отображения в списке чатов и превью переписки.
     *
     * @param chatId идентификатор чат-комнаты
     * @return последнее сообщение или 404 если сообщений нет
     */
    @GetMapping("/chat/{chatId}/last")
    public ResponseEntity<MessageDto> getLastMessage(@PathVariable Integer chatId) {
        log.info("Получение последнего сообщения в чате ID: {}", chatId);
        MessageDto lastMessage = messageService.getLastMessageByChatId(chatId);
        return ResponseEntity.ok(lastMessage);
    }

    /**
     * Обновление статуса прочтения для группы сообщений
     *
     * <p>Отмечает все сообщения в указанном чате как прочитанные
     * для текущего пользователя. Используется при открытии чата
     * пользователем.
     *
     * @param chatId идентификатор чат-комнаты
     * @return ответ с подтверждением обновления
     */
    @PutMapping("/chat/{chatId}/mark-as-read")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable Integer chatId) {
        log.info("Отметка сообщений в чате ID: {} как прочитанные", chatId);
        messageService.markMessagesAsRead(chatId);
        return ResponseEntity.ok().build();
    }

    /**
     * Получение количества сообщений в чате
     *
     * <p>Возвращает общее количество сообщений в указанной чат-комнате.
     * Используется для отображения статистики и информации о чате.
     *
     * @param chatId идентификатор чат-комнаты
     * @return количество сообщений в чате
     */
    @GetMapping("/chat/{chatId}/count")
    public ResponseEntity<Long> getMessageCount(@PathVariable Integer chatId) {
        log.info("Получение количества сообщений в чате ID: {}", chatId);
        Long count = messageService.getMessageCount(chatId);
        return ResponseEntity.ok(count);
    }
}