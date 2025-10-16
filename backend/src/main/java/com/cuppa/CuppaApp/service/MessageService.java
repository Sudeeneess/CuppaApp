package com.cuppa.CuppaApp.service;

import com.cuppa.CuppaApp.dto.MessageDto;
import com.cuppa.CuppaApp.entity.ChatRoom;
import com.cuppa.CuppaApp.entity.Message;
import com.cuppa.CuppaApp.entity.User;
import com.cuppa.CuppaApp.repository.ChatRoomRepository;
import com.cuppa.CuppaApp.repository.UserRepository;
import com.cuppa.CuppaApp.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с сообщениями в чат-комнатах
 *
 * <p>Обеспечивает бизнес-логику для работы с сообщениями:
 * создание, редактирование, поиск и управление статусами сообщений.
 * Конвертирует сущности в DTO и обратно для безопасной передачи данных.
 *
 * @author Petr Panteev, Walerya Pleskova
 * @version 1.1
 * @since 15.10.2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    /**
     * Получение сообщения по идентификатору
     *
     * @param id идентификатор сообщения
     * @return DTO сообщения
     * @throws RuntimeException если сообщение не найдено
     */
    @Transactional(readOnly = true)
    public MessageDto getMessageById(Integer id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Сообщение не найдено с ID: " + id));
        return MessageDto.fromEntity(message);
    }

    /**
     * Получение истории сообщений чата с пагинацией
     *
     * @param chatId   идентификатор чат-комнаты
     * @param pageable параметры пагинации
     * @return страница с DTO сообщений
     */
    @Transactional(readOnly = true)
    public Page<MessageDto> getMessagesByChatId(Integer chatId, Pageable pageable) {
        return messageRepository.findByChatRoomIdOrderBySentAtDesc(chatId, pageable)
                .map(MessageDto::fromEntity);
    }

    /**
     * Создает и сохраняет новое сообщение в указанном чате.
     *
     * <p>Метод выполняет следующие операции:
     * <ol>
     *   <li>Валидирует существование чата и отправителя в базе данных</li>
     *   <li>Создает новую сущность сообщения на основе DTO</li>
     *   <li>Устанавливает тип сообщения с обработкой невалидных значений</li>
     *   <li>Сохраняет сообщение в базу данных</li>
     *   <li>Обновляет информацию о последнем сообщении в чате</li>
     *   <li>Возвращает DTO созданного сообщения</li>
     * </ol>
     *
     * <p><b>Валидация:</b>
     * <ul>
     *   <li>Чат должен существовать в базе данных</li>
     *   <li>Отправитель должен существовать в базе данных</li>
     *   <li>Тип сообщения валидируется, при ошибке устанавливается TEXT по умолчанию</li>
     * </ul>
     *
     * @param messageDto DTO с данными для создания сообщения. Должен содержать:
     *                  <ul>
     *                    <li>{@code chatRoomId} - идентификатор чата (обязательно)</li>
     *                    <li>{@code senderId} - идентификатор отправителя (обязательно)</li>
     *                    <li>{@code content} - текст сообщения</li>
     *                    <li>{@code messageType} - тип сообщения (TEXT, IMAGE, FILE, etc.)</li>
     *                  </ul>
     *
     * @return MessageDto созданного сообщения с заполненными полями:
     *         <ul>
     *           <li>ID сообщения</li>
     *           <li>Дата и время отправки</li>
     *           <li>Имя отправителя</li>
     *           <li>Статусы доставки и прочтения</li>
     *         </ul>
     *
     * @throws RuntimeException если чат не найден с указанным ID
     * @throws RuntimeException если пользователь не найден с указанным ID
     *
     * @example
     * <pre>{@code
     * // Создание текстового сообщения
     * MessageDto messageDto = new MessageDto();
     * messageDto.setChatRoomId(1);
     * messageDto.setSenderId(123);
     * messageDto.setContent("Привет, как дела?");
     * messageDto.setMessageType("TEXT");
     *
     * MessageDto createdMessage = messageService.createMessage(messageDto);
     *
     * // Результат:
     * // MessageDto{
     * //   id=456,
     * //   content="Привет, как дела?",
     * //   senderId=123,
     * //   senderName="Иван Иванов",
     * //   chatRoomId=1,
     * //   messageType="TEXT",
     * //   sentAt=2025-01-16T18:30:00,
     * //   isEdited=false,
     * //   deliveredAt=null,
     * //   readAt=null
     * // }
     * }</pre>
     *
     * @see Message
     * @see MessageDto
     * @see ChatRoom
     * @see User
     * @see Message.MessageType
     *
     * @implNote Временная метка отправки устанавливается автоматически как текущее время сервера
     * @implNote При невалидном типе сообщения автоматически устанавливается TEXT
     * @implNote Информация о последнем сообщении в чате обновляется автоматически
     */
    public MessageDto createMessage(MessageDto messageDto) {
        log.info("Создание сообщения в чате {} от пользователя {}",
                messageDto.getChatRoomId(), messageDto.getSenderId());

        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Чат не найден с ID: " + messageDto.getChatRoomId()));

        User sender = userRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + messageDto.getSenderId()));

        Message message = new Message();
        message.setContent(messageDto.getContent());
        message.setChatRoom(chatRoom);
        message.setSender(sender);

        // Безопасно устанавливаем тип сообщения
        try {
            message.setMessageType(Message.MessageType.valueOf(messageDto.getMessageType()));
        } catch (IllegalArgumentException e) {
            message.setMessageType(Message.MessageType.TEXT);
        }

        message.setSentAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        // Обновляем информацию о последнем сообщении в чате
        chatRoomService.updateLastMessageInfo(
                messageDto.getChatRoomId(),
                messageDto.getContent(),
                messageDto.getSenderId()
        );

        log.info("Сообщение создано с ID: {}", savedMessage.getId());
        return MessageDto.fromEntity(savedMessage);
    }

    /**
     * Редактирование существующего сообщения
     *
     * @param id         идентификатор сообщения
     * @param messageDto DTO с обновленными данными
     * @return обновленное DTO сообщения
     * @throws RuntimeException если сообщение не найдено
     */
    public MessageDto updateMessage(Integer id, MessageDto messageDto) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Сообщение не найдено с ID: " + id));

        message.setContent(messageDto.getContent());
        message.setIsEdited(true);
        message.setEditedAt(LocalDateTime.now());

        Message updatedMessage = messageRepository.save(message);
        return MessageDto.fromEntity(updatedMessage);
    }

    /**
     * Удаление сообщения
     *
     * @param id идентификатор сообщения
     * @throws RuntimeException если сообщение не найдено
     */
    public void deleteMessage(Integer id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Сообщение не найдено с ID: " + id));

        messageRepository.delete(message);
    }

    /**
     * Поиск сообщений по содержимому в указанном чате
     *
     * @param chatId   идентификатор чат-комнаты
     * @param query    текст для поиска
     * @param pageable параметры пагинации
     * @return страница с найденными сообщениями
     */
    @Transactional(readOnly = true)
    public Page<MessageDto> searchMessages(Integer chatId, String query, Pageable pageable) {
        return messageRepository.findByChatRoomIdAndContentContainingIgnoreCase(chatId, query, pageable)
                .map(MessageDto::fromEntity);
    }

    /**
     * Создает сообщение и отправляет через WebSocket
     */
    public MessageDto createAndBroadcastMessage(MessageDto messageDto) {
        MessageDto savedMessage = createMessage(messageDto);

        // Отправляем через WebSocket всем подписчикам
        messagingTemplate.convertAndSend(
                "/topic/chat/" + messageDto.getChatRoomId(),
                savedMessage
        );

        return savedMessage;
    }

    /**
     * Получение сообщений определенного типа в чате
     *
     * @param chatId      идентификатор чат-комнаты
     * @param messageType тип сообщения
     * @return список сообщений указанного типа
     */
    @Transactional(readOnly = true)
    public List<MessageDto> getMessagesByType(Integer chatId, String messageType) {
        Message.MessageType type = Message.MessageType.valueOf(messageType.toUpperCase());
        return messageRepository.findByChatRoomIdAndMessageType(chatId, type).stream()
                .map(MessageDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Получение последнего сообщения в чате
     *
     * @param chatId идентификатор чат-комнаты
     * @return последнее сообщение или null если сообщений нет
     */
    @Transactional(readOnly = true)
    public MessageDto getLastMessageByChatId(Integer chatId) {
        Message lastMessage = messageRepository.findLastMessageByChatId(chatId);
        return lastMessage != null ? MessageDto.fromEntity(lastMessage) : null;
    }

    /**
     * Обновление статуса прочтения для группы сообщений
     *
     * @param chatId идентификатор чат-комнаты
     */
    public void markMessagesAsRead(Integer chatId) {
        // В реальном приложении здесь будет логика отметки сообщений как прочитанных
        // для конкретного пользователя с использованием времени lastReadAt
        log.info("Сообщения в чате ID: {} отмечены как прочитанные", chatId);
    }

    /**
     * Получение количества сообщений в чате
     *
     * @param chatId идентификатор чат-комнаты
     * @return количество сообщений в чате
     */
    @Transactional(readOnly = true)
    public Long getMessageCount(Integer chatId) {
        return messageRepository.countByChatRoomId(chatId);
    }
}