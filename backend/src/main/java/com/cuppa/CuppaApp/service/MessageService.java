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
import org.springframework.context.annotation.Lazy; // ← ИСПРАВЛЕННЫЙ ИМПОРТ
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;


    // ... остальные методы без изменений ...
    @Transactional(readOnly = true)
    public MessageDto getMessageById(Integer id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Сообщение не найдено с ID: " + id));
        return MessageDto.fromEntity(message);
    }

    @Transactional(readOnly = true)
    public Page<MessageDto> getMessagesByChatId(Integer chatId, Pageable pageable) {
        return messageRepository.findByChatRoomIdOrderBySentAtDesc(chatId, pageable)
                .map(MessageDto::fromEntity);
    }

    public MessageDto createMessage(MessageDto messageDto) {
        log.info("Создание нового сообщения в чате ID: {}", messageDto.getChatRoomId());

        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Чат не найден с ID: " + messageDto.getChatRoomId()));

        User sender = userRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Пользователь (отправитель) не найден с ID: " + messageDto.getSenderId()));

        Message message = new Message();
        message.setChatRoom(chatRoom);
        message.setSender(sender);
        message.setContent(messageDto.getContent());

        LocalDateTime now = LocalDateTime.now();
        message.setSentAt(now);
        message.setDeliveredAt(now);
        message.setReadAt(now);

        try {
            Message.MessageType type = Message.MessageType.valueOf(messageDto.getMessageType().toUpperCase());
            message.setMessageType(type);
        } catch (IllegalArgumentException e) {
            log.warn("Неверный тип сообщения '{}'. Установлен тип TEXT.", messageDto.getMessageType());
            message.setMessageType(Message.MessageType.TEXT);
        }

        Message savedMessage = messageRepository.save(message);
        log.info("Сообщение ID: {} успешно сохранено", savedMessage.getId());

        // ЗАМЕНИТЕ вызов chatRoomService на прямой вызов репозитория
        chatRoomRepository.updateLastMessageInfo(
                chatRoom.getId(),
                savedMessage.getContent(),
                savedMessage.getSender().getId(),
                LocalDateTime.now()
        );

        return MessageDto.fromEntity(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessagesByChatIdAndType(Integer chatId, String messageType) {
        Message.MessageType type = Message.MessageType.valueOf(messageType.toUpperCase());
        return messageRepository.findByChatRoomIdAndMessageType(chatId, type).stream()
                .map(MessageDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MessageDto getLastMessageByChatId(Integer chatId) {
        Message lastMessage = messageRepository.findLastMessageByChatId(chatId);
        return lastMessage != null ? MessageDto.fromEntity(lastMessage) : null;
    }

    public void markMessagesAsRead(Integer chatId) {
        log.info("Сообщения в чате ID: {} отмечены как прочитанные", chatId);
    }

    @Transactional(readOnly = true)
    public Long getMessageCount(Integer chatId) {
        return messageRepository.countByChatRoomId(chatId);
    }

    @Transactional(readOnly = true)
    public Integer countUnreadMessages(Integer chatRoomId, LocalDateTime lastReadTime, Integer currentUserId) {
        if (lastReadTime == null) {
            return messageRepository.countByChatRoomIdAndSenderIdNot(chatRoomId, currentUserId).intValue();
        }
        return messageRepository.countUnreadMessagesAfterTime(chatRoomId, lastReadTime, currentUserId).intValue();
    }

    @Transactional
    public MessageDto updateMessage(Integer messageId, MessageDto updatedMessageDto, Integer senderId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Сообщение не найдено с ID: " + messageId));

        if (!message.getSender().getId().equals(senderId)) {
            throw new AccessDeniedException("У вас нет прав для редактирования этого сообщения.");
        }

        message.setContent(updatedMessageDto.getContent());
        message.setIsEdited(true);
        message.setEditedAt(LocalDateTime.now());

        message = messageRepository.save(message);
        return MessageDto.fromEntity(message);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessagesByType(Integer chatId, String messageType) {
        Message.MessageType type;
        try {
            type = Message.MessageType.valueOf(messageType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Попытка фильтрации по неизвестному типу сообщения: {}", messageType);
            return List.of();
        }

        return messageRepository.findByChatRoomIdAndMessageType(chatId, type).stream()
                .map(MessageDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMessage(Integer messageId, Integer currentUserId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Сообщение не найдено с ID: " + messageId));

        if (!message.getSender().getId().equals(currentUserId)) {
            throw new AccessDeniedException("У вас нет прав для удаления этого сообщения.");
        }

        messageRepository.delete(message);
        log.info("Сообщение ID: {} удалено пользователем ID: {}", messageId, currentUserId);
    }

    @Transactional(readOnly = true)
    public Page<MessageDto> searchMessages(Integer chatId, String query, Pageable pageable) {
        Page<Message> messagePage = messageRepository.findByChatRoomIdAndContentContainingIgnoreCase(chatId, query, pageable);
        return messagePage.map(MessageDto::fromEntity);
    }
}