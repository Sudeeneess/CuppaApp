package com.cuppa.CuppaApp.controllers;

import com.cuppa.CuppaApp.dto.MessageDto;
import com.cuppa.CuppaApp.dto.MessageReadEventDto;
import com.cuppa.CuppaApp.dto.TypingEventDto;
import com.cuppa.CuppaApp.dto.WebSocketMessageDto;
import com.cuppa.CuppaApp.service.MessageService;
import com.cuppa.CuppaApp.service.WebSocketSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;

/**
 * WebSocket контроллер для обработки реального времени чата.
 * Обеспечивает двустороннюю коммуникацию между клиентами через WebSocket соединения.
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Отправка и получение сообщений в реальном времени</li>
 *   <li>Уведомления о наборе текста (typing indicators)</li>
 *   <li>Отслеживание прочтения сообщений</li>
 *   <li>Управление подписками на чаты</li>
 * </ul>
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @see MessageService
 * @see SimpMessagingTemplate
 * @see WebSocketMessageDto
 * @since 2025-10-28
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final WebSocketSecurityService webSocketSecurityService;

    /**
     * Обрабатывает отправку нового сообщения в указанный чат.
     * Сохраняет сообщение в базу данных и рассылает его всем подписанным клиентам.
     *
     * <p><b>Маршрут:</b> {@code /app/chat/{chatId}/send}
     * <p><b>Назначение:</b> {@code /topic/chat/{chatId}}
     *
     * @param messageDto DTO сообщения от клиента
     * @param chatId     идентификатор чат-комнаты из пути URL
     * @return WebSocketMessageDto с сохраненным сообщением для рассылки подписчикам
     * @example <pre>{@code
     * // Клиент отправляет:
     * {
     *   "senderId": 123,
     *   "content": "Привет всем!",
     *   "messageType": "TEXT"
     * }
     *
     * // Сервер рассылает:
     * {
     *   "type": "CHAT_MESSAGE",
     *   "chatRoomId": 1,
     *   "senderId": 123,
     *   "senderName": "User123",
     *   "content": "Привет всем!",
     *   "timestamp": "2025-01-16T18:30:00",
     *   "payload": { ... полное сообщение ... }
     * }
     * }</pre>
     */
    @MessageMapping("/chat/{chatId}/send")
    @SendTo("/topic/chat/{chatId}")
    public WebSocketMessageDto sendMessage(@Payload MessageDto messageDto, @DestinationVariable Integer chatId, Principal principal  //  Добавляем Principal для аутентификации
    ) {

        log.info("WebSocket: Получено сообщение для чата {} от пользователя {} (аутентифицированный: {})", chatId, messageDto.getSenderId(), principal.getName());

        try {
            //  ПРОВЕРКА БЕЗОПАСНОСТИ
            webSocketSecurityService.validateSender(messageDto.getSenderId(), principal.getName());
            webSocketSecurityService.validateChatAccess(chatId, principal.getName());

            // Устанавливаем chatId из пути
            messageDto.setChatRoomId(chatId);

            // Сохраняем сообщение в базу данных
            MessageDto savedMessage = messageService.createMessage(messageDto);

            // Создаем WebSocket сообщение для рассылки
            WebSocketMessageDto webSocketMessage = new WebSocketMessageDto();
            webSocketMessage.setType(WebSocketMessageDto.MessageType.CHAT_MESSAGE);
            webSocketMessage.setChatRoomId(chatId);
            webSocketMessage.setSenderId(savedMessage.getSenderId());
            webSocketMessage.setSenderName(savedMessage.getSenderName());
            webSocketMessage.setContent(savedMessage.getContent());
            webSocketMessage.setTimestamp(LocalDateTime.now());
            webSocketMessage.setPayload(savedMessage);

            log.info("WebSocket: Сообщение сохранено и отправлено в чат {}", chatId);
            return webSocketMessage;

        } catch (AccessDeniedException e) {
            log.warn("WebSocket: Отказано в доступе для чата {}: {}", chatId, e.getMessage());

            WebSocketMessageDto errorMessage = new WebSocketMessageDto();
            errorMessage.setType(WebSocketMessageDto.MessageType.CHAT_MESSAGE);
            errorMessage.setChatRoomId(chatId);
            errorMessage.setContent("Ошибка доступа: " + e.getMessage());
            errorMessage.setTimestamp(LocalDateTime.now());
            return errorMessage;

        } catch (Exception e) {
            log.error("WebSocket: Ошибка при обработке сообщения для чата {}", chatId, e);

            WebSocketMessageDto errorMessage = new WebSocketMessageDto();
            errorMessage.setType(WebSocketMessageDto.MessageType.CHAT_MESSAGE);
            errorMessage.setChatRoomId(chatId);
            errorMessage.setContent("Ошибка при отправке сообщения");
            errorMessage.setTimestamp(LocalDateTime.now());
            return errorMessage;
        }
    }

    /**
     * Обрабатывает уведомления о наборе текста (typing indicators).
     * Рассылает информацию о том, что пользователь начал/закончил печатать.
     *
     * <p><b>Маршрут:</b> {@code /app/chat/{chatId}/typing}
     * <p><b>Назначение:</b> {@code /topic/chat/{chatId}/typing}
     *
     * @param chatId      идентификатор чат-комнаты
     * @param typingEvent событие набора текста с информацией о пользователе
     * @example <pre>{@code
     * // Клиент отправляет при начале набора:
     * {
     *   "userId": 123,
     *   "userName": "User123",
     *   "isTyping": true
     * }
     *
     * // Сервер рассылает:
     * {
     *   "type": "TYPING",
     *   "chatRoomId": 1,
     *   "senderId": 123,
     *   "timestamp": "2025-01-16T18:30:00",
     *   "payload": { ... typing event ... }
     * }
     * }</pre>
     */
    @MessageMapping("/chat/{chatId}/typing")
    public void handleTyping(@DestinationVariable Integer chatId, @Payload TypingEventDto typingEvent) {

        log.debug("WebSocket: Пользователь {} печатает в чате {}", typingEvent.getUserId(), chatId);

        WebSocketMessageDto typingMessage = new WebSocketMessageDto();
        typingMessage.setType(WebSocketMessageDto.MessageType.TYPING);
        typingMessage.setChatRoomId(chatId);
        typingMessage.setSenderId(typingEvent.getUserId());
        typingMessage.setTimestamp(LocalDateTime.now());
        typingMessage.setPayload(typingEvent);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/typing", typingMessage);
    }

    /**
     * Обрабатывает уведомления о прочтении сообщений.
     * Уведомляет других участников чата, что пользователь прочитал сообщения.
     *
     * <p><b>Маршрут:</b> {@code /app/chat/{chatId}/read}
     * <p><b>Назначение:</b> {@code /topic/chat/{chatId}/read}
     *
     * @param chatId    идентификатор чат-комнаты
     * @param readEvent событие прочтения с информацией о пользователе и сообщении
     * @example <pre>{@code
     * // Клиент отправляет:
     * {
     *   "userId": 123,
     *   "userName": "User123",
     *   "lastReadMessageId": 456
     * }
     *
     * // Сервер рассылает:
     * {
     *   "type": "MESSAGE_READ",
     *   "chatRoomId": 1,
     *   "senderId": 123,
     *   "timestamp": "2025-01-16T18:30:00",
     *   "payload": { ... read event ... }
     * }
     * }</pre>
     */
    @MessageMapping("/chat/{chatId}/read")
    public void handleMessageRead(@DestinationVariable Integer chatId, @Payload MessageReadEventDto readEvent) {

        log.debug("WebSocket: Пользователь {} прочитал сообщения в чате {}", readEvent.getUserId(), chatId);

        WebSocketMessageDto readMessage = new WebSocketMessageDto();
        readMessage.setType(WebSocketMessageDto.MessageType.MESSAGE_READ);
        readMessage.setChatRoomId(chatId);
        readMessage.setSenderId(readEvent.getUserId());
        readMessage.setTimestamp(LocalDateTime.now());
        readMessage.setPayload(readEvent);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/read", readMessage);
    }

    /**
     * Обрабатывает начальную подписку клиента на чат.
     * Вызывается автоматически при подключении клиента к WebSocket каналу чата.
     *
     * <p><b>Маршрут подписки:</b> {@code /topic/chat/{chatId}}
     * <p><b>Ответ:</b> приветственное сообщение отправляется только подписавшемуся клиенту
     *
     * @param chatId идентификатор чат-комнаты
     * @return WebSocketMessageDto приветственное сообщение об успешном подключении
     * @example <pre>{@code
     * // Клиент подписывается на /topic/chat/1
     * // Сервер отправляет ответ:
     * {
     *   "type": "CHAT_MESSAGE",
     *   "chatRoomId": 1,
     *   "content": "Подключен к чату в реальном времени",
     *   "timestamp": "2025-01-16T18:30:00"
     * }
     * }</pre>
     */
    @SubscribeMapping("/chat/{chatId}")
    public WebSocketMessageDto handleChatSubscription(@DestinationVariable Integer chatId) {
        log.info("WebSocket: Клиент подписался на чат {}", chatId);

        WebSocketMessageDto welcomeMessage = new WebSocketMessageDto();
        welcomeMessage.setType(WebSocketMessageDto.MessageType.CHAT_MESSAGE);
        welcomeMessage.setChatRoomId(chatId);
        welcomeMessage.setContent("Подключен к чату в реальном времени");
        welcomeMessage.setTimestamp(LocalDateTime.now());

        return welcomeMessage;
    }
}