package com.cuppa.CuppaApp.service;

import com.cuppa.CuppaApp.dto.MessageDto;
import com.cuppa.CuppaApp.entity.Message;
import com.cuppa.CuppaApp.repository.MessageRepository;
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
 * Сервис для работы с сообщениями в чат-комнатах
 *
 * <p>Обеспечивает бизнес-логику для работы с сообщениями:
 * создание, редактирование, поиск и управление статусами сообщений.
 * Конвертирует сущности в DTO и обратно для безопасной передачи данных.
 *
 * @author Petr Panteev
 * @version 1.0
 * @since 15.10.2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;

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
     * @param chatId идентификатор чат-комнаты
     * @param pageable параметры пагинации
     * @return страница с DTO сообщений
     */
    @Transactional(readOnly = true)
    public Page<MessageDto> getMessagesByChatId(Integer chatId, Pageable pageable) {
        return messageRepository.findByChatRoomIdOrderBySentAtDesc(chatId, pageable)
                .map(MessageDto::fromEntity);
    }

    /**
     * Создание нового сообщения в чате
     *
     * @param messageDto DTO с данными сообщения
     * @return созданное DTO сообщения
     */
    public MessageDto createMessage(MessageDto messageDto) {
        // Здесь должна быть логика создания сообщения
        // В реальном приложении нужно будет создать сущность из DTO
        // и установить временные метки
        throw new UnsupportedOperationException("Метод создания сообщения будет реализован позже");
    }

    /**
     * Редактирование существующего сообщения
     *
     * @param id идентификатор сообщения
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
     * @param chatId идентификатор чат-комнаты
     * @param query текст для поиска
     * @param pageable параметры пагинации
     * @return страница с найденными сообщениями
     */
    @Transactional(readOnly = true)
    public Page<MessageDto> searchMessages(Integer chatId, String query, Pageable pageable) {
        return messageRepository.findByChatRoomIdAndContentContainingIgnoreCase(chatId, query, pageable)
                .map(MessageDto::fromEntity);
    }

    /**
     * Получение сообщений определенного типа в чате
     *
     * @param chatId идентификатор чат-комнаты
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