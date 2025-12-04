package com.cuppa.CuppaApp.controllers;

import com.cuppa.CuppaApp.dto.MessageDto;
import com.cuppa.CuppaApp.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления сообщениями в чат-комнатах
 *
 * <p>Обеспечивает REST API для работы с сообщениями системы мессенджера:
 * отправка сообщений, получение истории сообщений, управление статусами
 * доставки и прочтения, поиск и фильтрация сообщений.
 *
 * <p>Все методы защищены проверками безопасности, включая Rate Limiting,
 * защиту от XSS атак, валидацию размера файлов, мониторинг аномальной активности
 * и проверки прав доступа.
 *
 * @author Petr Panteev
 * @author Walerya Pleskova
 * @since 2025-11-29
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SecurityService securityService;
    private final RateLimitService rateLimitService;
    private final XSSProtectionService xssProtectionService;
    private final AnomalyDetectionService anomalyDetectionService;

    /**
     * Получение сообщения по идентификатору
     *
     * <p>Возвращает сообщение по его уникальному идентификатору.
     * Перед возвратом проверяет, что текущий пользователь имеет
     * доступ к чату, в котором находится сообщение.
     *
     * @param id             идентификатор сообщения
     * @param authentication объект аутентификации Spring Security
     * @return DTO сообщения со статусом 200 OK или 403 Forbidden если нет доступа
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMessage(@PathVariable Integer id, Authentication authentication) {
        log.info("Получение сообщения с ID: {}", id);

        String userEmail = authentication.getName();

        // ПРОВЕРКА RATE LIMITING
        if (!rateLimitService.isAllowed(userEmail)) {
            log.warn("Rate limit exceeded for user: {}", userEmail);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests. Please try again later.");
        }

        try {
            MessageDto message = messageService.getMessageById(id);

            // ПРОВЕРКА: пользователь имеет доступ к чату этого сообщения
            if (!securityService.hasAccessToChat(message.getChatRoomId())) {
                log.warn("Отказано в доступе к сообщению {}: пользователь не состоит в чате {}", id, message.getChatRoomId());

                // МОНИТОРИНГ АНОМАЛЬНОЙ АКТИВНОСТИ
                anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.ACCESS_DENIED, "message_" + id, "Attempted to access message in unauthorized chat: " + message.getChatRoomId());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // МОНИТОРИНГ АКТИВНОСТИ
            anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.READ_MESSAGE, "message_" + id, "Accessed message in chat: " + message.getChatRoomId());

            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            log.error("Ошибка при получении сообщения {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Получение истории сообщений чата с пагинацией
     *
     * <p>Возвращает страницу сообщений для указанной чат-комнаты,
     * отсортированных по времени отправки в порядке убывания
     * (от новых к старым). Проверяет права доступа к чату.
     *
     * @param chatId         идентификатор чат-комнаты
     * @param pageable       параметры пагинации
     * @param authentication объект аутентификации Spring Security
     * @return страница с DTO сообщений или 403 Forbidden если нет доступа
     */
    @GetMapping("/chat/{chatId}")
    public ResponseEntity<?> getChatMessages(@PathVariable Integer chatId, @PageableDefault(size = 50, sort = "sentAt") Pageable pageable, Authentication authentication) {

        log.info("Получение сообщений чата с ID: {}, page: {}, size: {}", chatId, pageable.getPageNumber(), pageable.getPageSize());

        String userEmail = authentication.getName();

        // ПРОВЕРКА RATE LIMITING
        if (!rateLimitService.isAllowed(userEmail)) {
            log.warn("Rate limit exceeded for user: {}", userEmail);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests. Please try again later.");
        }

        try {
            // ПРОВЕРКА: пользователь имеет доступ к этому чату
            if (!securityService.hasAccessToChat(chatId)) {
                log.warn("Попытка доступа к чужому чату: {}", chatId);

                // МОНИТОРИНГ АНОМАЛЬНОЙ АКТИВНОСТИ
                anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.ACCESS_DENIED, "chat_" + chatId, "Attempted to access unauthorized chat");

                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // МОНИТОРИНГ АКТИВНОСТИ
            anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.CHAT_ACCESS, "chat_" + chatId, "Accessed chat messages - page: " + pageable.getPageNumber());

            Page<MessageDto> messages = messageService.getMessagesByChatId(chatId, pageable);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            log.error("Ошибка при получении сообщений чата {}: {}", chatId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Отправка нового сообщения в чат
     *
     * <p>Создает новое сообщение в указанной чат-комнате от имени
     * указанного пользователя. Включает многоуровневую защиту:
     * Rate Limiting, XSS защиту, валидацию размера и прав доступа.
     *
     * @param messageDto     DTO с данными сообщения
     * @param authentication объект аутентификации Spring Security
     * @return созданное DTO сообщения или ошибку при нарушении безопасности
     */
    @PostMapping
    public ResponseEntity<?> sendMessage(@Valid @RequestBody MessageDto messageDto, Authentication authentication) {

        String userEmail = authentication.getName();
        log.info("Отправка нового сообщения в чат ID: {} от пользователя ID: {}", messageDto.getChatRoomId(), messageDto.getSenderId());

        // ПРОВЕРКА RATE LIMITING
        if (!rateLimitService.isAllowed(userEmail)) {
            log.warn("Rate limit exceeded for user: {}", userEmail);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests. Please try again later.");
        }

        try {
            // ПРОВЕРКА 1: пользователь отправляет от СВОЕГО имени
            if (!securityService.isCurrentUser(messageDto.getSenderId())) {
                log.warn("Попытка отправки сообщения от чужого имени: {}", messageDto.getSenderId());

                // МОНИТОРИНГ АНОМАЛЬНОЙ АКТИВНОСТИ
                anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.ACCESS_DENIED, "chat_" + messageDto.getChatRoomId(), "Attempted to send message as another user: " + messageDto.getSenderId());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot send messages as another user");
            }

            // ПРОВЕРКА 2: пользователь имеет доступ к целевому чату
            if (!securityService.hasAccessToChat(messageDto.getChatRoomId())) {
                log.warn("Попытка отправки сообщения в чужой чат: {}", messageDto.getChatRoomId());

                // МОНИТОРИНГ АНОМАЛЬНОЙ АКТИВНОСТИ
                anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.ACCESS_DENIED, "chat_" + messageDto.getChatRoomId(), "Attempted to send message to unauthorized chat");

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No access to this chat");
            }

            // ПРОВЕРКА 3: XSS ЗАЩИТА
            if (xssProtectionService.hasXSSThreats(messageDto.getContent())) {
                log.warn("Обнаружена XSS угроза в сообщении от пользователя: {}", userEmail);

                // МОНИТОРИНГ АНОМАЛЬНОЙ АКТИВНОСТИ
                anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.SEND_MESSAGE, "chat_" + messageDto.getChatRoomId(), "XSS threat detected in message content");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Message contains potentially dangerous content");
            }

            // ПРОВЕРКА 4: ВАЛИДАЦИЯ РАЗМЕРА И СОДЕРЖИМОГО
            ResponseEntity<?> validationResult = validateMessageContent(messageDto);
            if (validationResult != null) {
                return validationResult;
            }

            // ОЧИСТКА КОНТЕНТА ОТ XSS
            String sanitizedContent = xssProtectionService.sanitize(messageDto.getContent());
            messageDto.setContent(sanitizedContent);

            MessageDto createdMessage = messageService.createMessage(messageDto);

            // МОНИТОРИНГ АКТИВНОСТИ - УСПЕШНАЯ ОТПРАВКА
            anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.SEND_MESSAGE, "chat_" + messageDto.getChatRoomId(), "Message sent successfully - ID: " + createdMessage.getId());

            return ResponseEntity.ok(createdMessage);

        } catch (RuntimeException e) {
            log.error("Ошибка при отправке сообщения: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending message: " + e.getMessage());
        }
    }

    /**
     * Обновление (редактирование) сообщения.
     * Требует аутентификации: только отправитель может редактировать свое сообщение.
     *
     * @param id ID сообщения
     * @param messageDto DTO с новым контентом
     * @return Обновленный MessageDto
     */
    @PatchMapping("/{id}")
    public ResponseEntity<MessageDto> updateMessage(
            @PathVariable Integer id,
            @Valid @RequestBody MessageDto messageDto) {
        try {
            Integer currentUserId = securityService.getCurrentUser().getId();
            MessageDto updatedMessage = messageService.updateMessage(id, messageDto, currentUserId);
            return ResponseEntity.ok(updatedMessage);
        } catch (Exception e) {
            log.error("Ошибка при обновлении сообщения ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Удаление сообщения (мягкое или жесткое).
     * Требует аутентификации: только отправитель может удалить свое сообщение.
     *
     * @param id ID сообщения
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Integer id) {
        try {
            Integer currentUserId = securityService.getCurrentUser().getId();
            messageService.deleteMessage(id, currentUserId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Ошибка при удалении сообщения ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Поиск сообщений по содержимому в указанном чате
     *
     * <p>Выполняет полнотекстовый поиск сообщений, содержащих
     * указанный текст в содержимом. Поиск не зависит от регистра.
     * Проверяет права доступа к целевому чату. Включает защиту
     * от SQL-инъекций через валидацию и очистку входных данных.
     *
     * @param chatId         идентификатор чат-комнаты
     * @param query          текст для поиска
     * @param pageable       параметры пагинации
     * @param authentication объект аутентификации Spring Security
     * @return страница с найденными сообщениями или 403 Forbidden если нет доступа
     */
    @GetMapping("/chat/{chatId}/search")
    public ResponseEntity<?> searchMessages(@PathVariable Integer chatId, @RequestParam String query, @PageableDefault(size = 20) Pageable pageable, Authentication authentication) {

        String userEmail = authentication.getName();

        // ПРОВЕРКА RATE LIMITING
        if (!rateLimitService.isAllowed(userEmail)) {
            log.warn("Rate limit exceeded for user: {}", userEmail);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests. Please try again later.");
        }

        try {
            // ПРОВЕРКА доступа к чату
            if (!securityService.hasAccessToChat(chatId)) {
                log.warn("Попытка поиска в чужом чате: {}", chatId);

                // МОНИТОРИНГ АНОМАЛЬНОЙ АКТИВНОСТИ
                anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.ACCESS_DENIED, "chat_" + chatId, "Attempted to search in unauthorized chat");

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No access to this chat");
            }

            // ВАЛИДАЦИЯ: проверяем что запрос не пустой и не слишком длинный
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Search query cannot be empty");
            }

            if (query.length() > 100) {
                log.warn("Слишком длинный поисковый запрос: {}", query.length());
                return ResponseEntity.badRequest().body("Search query too long (max 100 characters)");
            }

            // ОЧИСТКА запроса для защиты от SQL-инъекций
            String cleanQuery = query.trim().replace("%", "\\%").replace("_", "\\_").replace("'", "''");

            log.info("Поиск сообщений в чате ID: {} по запросу: '{}'", chatId, cleanQuery);

            // МОНИТОРИНГ АКТИВНОСТИ
            anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.SEARCH_MESSAGES, "chat_" + chatId, "Search performed with query: " + cleanQuery.substring(0, Math.min(50, cleanQuery.length())));

            Page<MessageDto> messages = messageService.searchMessages(chatId, cleanQuery, pageable);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            log.error("Ошибка при поиске сообщений в чате {}: {}", chatId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Search error: " + e.getMessage());
        }
    }

    /**
     * Получение сообщений определенного типа в чате
     *
     * <p>Возвращает все сообщения указанного типа (TEXT, IMAGE, FILE и т.д.)
     * в рамках конкретной чат-комнаты. Проверяет права доступа к чату.
     *
     * @param chatId         идентификатор чат-комнаты
     * @param messageType    тип сообщения
     * @param authentication объект аутентификации Spring Security
     * @return список сообщений указанного типа или 403 Forbidden если нет доступа
     */
    @GetMapping("/chat/{chatId}/type/{messageType}")
    public ResponseEntity<?> getMessagesByType(@PathVariable Integer chatId, @PathVariable String messageType, Authentication authentication) {

        String userEmail = authentication.getName();

        // ПРОВЕРКА RATE LIMITING
        if (!rateLimitService.isAllowed(userEmail)) {
            log.warn("Rate limit exceeded for user: {}", userEmail);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests. Please try again later.");
        }

        try {
            // ПРОВЕРКА доступа к чату
            if (!securityService.hasAccessToChat(chatId)) {
                log.warn("Попытка получения сообщений из чужого чата: {}", chatId);

                // МОНИТОРИНГ АНОМАЛЬНОЙ АКТИВНОСТИ
                anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.ACCESS_DENIED, "chat_" + chatId, "Attempted to access messages by type in unauthorized chat");

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No access to this chat");
            }

            // ВАЛИДАЦИЯ типа сообщения
            if (messageType == null || messageType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Message type cannot be empty");
            }

            log.info("Получение сообщений типа '{}' в чате ID: {}", messageType, chatId);

            // МОНИТОРИНГ АКТИВНОСТИ
            anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.READ_MESSAGE, "chat_" + chatId, "Accessed messages by type: " + messageType);

            List<MessageDto> messages = messageService.getMessagesByType(chatId, messageType);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            log.error("Ошибка при получении сообщений по типу {} в чате {}: {}", messageType, chatId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving messages: " + e.getMessage());
        }
    }

    /**
     * Получение последнего сообщения в чате
     *
     * <p>Возвращает самое последнее сообщение в указанной чат-комнате
     * для отображения в списке чатов и превью переписки.
     * Проверяет права доступа к целевому чату.
     *
     * @param chatId         идентификатор чат-комнаты
     * @param authentication объект аутентификации Spring Security
     * @return последнее сообщение или 403 Forbidden если нет доступа
     */
    @GetMapping("/chat/{chatId}/last")
    public ResponseEntity<?> getLastMessage(@PathVariable Integer chatId, Authentication authentication) {

        String userEmail = authentication.getName();

        // ПРОВЕРКА RATE LIMITING
        if (!rateLimitService.isAllowed(userEmail)) {
            log.warn("Rate limit exceeded for user: {}", userEmail);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests. Please try again later.");
        }

        try {
            // ПРОВЕРКА доступа к чату
            if (!securityService.hasAccessToChat(chatId)) {
                log.warn("Попытка получения последнего сообщения из чужого чата: {}", chatId);

                // МОНИТОРИНГ АНОМАЛЬНОЙ АКТИВНОСТИ
                anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.ACCESS_DENIED, "chat_" + chatId, "Attempted to access last message in unauthorized chat");

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No access to this chat");
            }

            log.info("Получение последнего сообщения в чате ID: {}", chatId);

            // МОНИТОРИНГ АКТИВНОСТИ
            anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.READ_MESSAGE, "chat_" + chatId, "Accessed last message in chat");

            MessageDto lastMessage = messageService.getLastMessageByChatId(chatId);

            if (lastMessage == null) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(lastMessage);
        } catch (RuntimeException e) {
            log.error("Ошибка при получении последнего сообщения в чате {}: {}", chatId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving last message: " + e.getMessage());
        }
    }

    /**
     * Обновление статуса прочтения для группы сообщений
     *
     * <p>Отмечает все сообщения в указанном чате как прочитанные
     * для текущего пользователя. Используется при открытии чата
     * пользователем. Проверяет права доступа к чату.
     *
     * @param chatId         идентификатор чат-комнаты
     * @param authentication объект аутентификации Spring Security
     * @return ответ с подтверждением обновления или 403 Forbidden если нет доступа
     */
    @PutMapping("/chat/{chatId}/mark-as-read")
    public ResponseEntity<Map<String, Object>> markMessagesAsRead(@PathVariable Integer chatId, Authentication authentication) {

        String userEmail = authentication.getName();
        Map<String, Object> response = new HashMap<>();

        // ПРОВЕРКА RATE LIMITING
        if (!rateLimitService.isAllowed(userEmail)) {
            log.warn("Rate limit exceeded for user: {}", userEmail);
            response.put("status", "error");
            response.put("message", "Too many requests. Please try again later.");
            response.put("code", "RATE_LIMIT_EXCEEDED");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
        }

        try {
            // ПРОВЕРКА доступа к чату
            if (!securityService.hasAccessToChat(chatId)) {
                log.warn("Попытка отметки прочтения в чужом чате: {}", chatId);

                // МОНИТОРИНГ АНОМАЛЬНОЙ АКТИВНОСТИ
                anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.ACCESS_DENIED, "chat_" + chatId, "Attempted to mark messages as read in unauthorized chat");

                response.put("status", "error");
                response.put("message", "No access to this chat");
                response.put("code", "ACCESS_DENIED");
                response.put("chatId", chatId);
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            log.info("Отметка сообщений в чате ID: {} как прочитанные", chatId);

            // МОНИТОРИНГ АКТИВНОСТИ
            anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.READ_MESSAGE, "chat_" + chatId, "Marked all messages as read");

            messageService.markMessagesAsRead(chatId);

            // УСПЕШНЫЙ ОТВЕТ
            response.put("status", "success");
            response.put("message", "Сообщения отмечены как прочитанные");
            response.put("chatId", chatId);
            response.put("timestamp", LocalDateTime.now());
            response.put("userEmail", userEmail);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Ошибка при отметке сообщений как прочитанных в чате {}: {}", chatId, e.getMessage());

            response.put("status", "error");
            response.put("message", "Error marking messages as read: " + e.getMessage());
            response.put("code", "INTERNAL_ERROR");
            response.put("chatId", chatId);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Получение количества сообщений в чате
     *
     * <p>Возвращает общее количество сообщений в указанной чат-комнате.
     * Используется для отображения статистики и информации о чате.
     * Проверяет права доступа к целевому чату.
     *
     * @param chatId         идентификатор чат-комнаты
     * @param authentication объект аутентификации Spring Security
     * @return количество сообщений в чате или 403 Forbidden если нет доступа
     */
    @GetMapping("/chat/{chatId}/count")
    public ResponseEntity<?> getMessageCount(@PathVariable Integer chatId, Authentication authentication) {

        String userEmail = authentication.getName();

        // ПРОВЕРКА RATE LIMITING
        if (!rateLimitService.isAllowed(userEmail)) {
            log.warn("Rate limit exceeded for user: {}", userEmail);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests. Please try again later.");
        }

        try {
            // ПРОВЕРКА доступа к чату
            if (!securityService.hasAccessToChat(chatId)) {
                log.warn("Попытка получения количества сообщений из чужого чата: {}", chatId);

                // МОНИТОРИНГ АНОМАЛЬНОЙ АКТИВНОСТИ
                anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.ACCESS_DENIED, "chat_" + chatId, "Attempted to get message count from unauthorized chat");

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No access to this chat");
            }

            log.info("Получение количества сообщений в чате ID: {}", chatId);

            // МОНИТОРИНГ АКТИВНОСТИ
            anomalyDetectionService.recordUserAction(userEmail, AnomalyDetectionService.ActionType.READ_MESSAGE, "chat_" + chatId, "Retrieved message count");

            Long count = messageService.getMessageCount(chatId);
            return ResponseEntity.ok(count);
        } catch (RuntimeException e) {
            log.error("Ошибка при получении количества сообщений в чате {}: {}", chatId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving message count: " + e.getMessage());
        }
    }

    /**
     * Валидация размера контента в зависимости от типа сообщения
     *
     * @param messageDto DTO сообщения для валидации
     * @return ResponseEntity с ошибкой или null если валидация пройдена
     */
    private ResponseEntity<?> validateMessageContent(MessageDto messageDto) {
        String messageType = messageDto.getMessageType() != null ? messageDto.getMessageType() : "TEXT";
        String content = messageDto.getContent();

        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Message content cannot be empty");
        }

        int contentLength = content.length();
        String upperMessageType = messageType.toUpperCase();

        switch (upperMessageType) {
            case "TEXT":
                if (contentLength > 10000) {
                    return ResponseEntity.badRequest().body("Text message too long (max 10000 characters)");
                }
                break;

            case "IMAGE":
                if (contentLength > 500000) {
                    return ResponseEntity.badRequest().body("Image too large (max 500KB)");
                }
                break;

            case "FILE":
                if (contentLength > 100000) {
                    return ResponseEntity.badRequest().body("File metadata too large");
                }
                break;

            case "VOICE":
            case "VIDEO":
                if (contentLength > 2000) {
                    return ResponseEntity.badRequest().body("Media reference too long");
                }
                break;

            case "SYSTEM":
                if (contentLength > 1000) {
                    return ResponseEntity.badRequest().body("System message too long (max 1000 characters)");
                }
                break;

            default:
                return ResponseEntity.badRequest().body("Unsupported message type: " + messageType);
        }

        return null;
    }
}