package com.cuppa.CuppaApp.service;

import com.cuppa.CuppaApp.dto.ChatParticipantDto;
import com.cuppa.CuppaApp.dto.ChatRoomDto;
import com.cuppa.CuppaApp.entity.ChatParticipant;
import com.cuppa.CuppaApp.entity.ChatRoom;
import com.cuppa.CuppaApp.entity.User;
import com.cuppa.CuppaApp.repository.ChatParticipantRepository;
import com.cuppa.CuppaApp.repository.ChatRoomRepository;
import com.cuppa.CuppaApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy; // <-- ИСПРАВЛЕННЫЙ ИМПОРТ
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для бизнес-логики работы с чат-комнатами в веб-мессенджере Cuppa
 *
 * <p>Обеспечивает основную функциональность для создания, управления и поиска чат-комнат.
 * Содержит бизнес-правила и валидацию для операций с чатами.
 *
 * @author Petr Panteev
 * @version 1.0
 * @since 14.10.2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantService chatParticipantService;
    @Lazy // <-- Использует правильный @Lazy для отложенной инициализации
    private final MessageService messageService;
    private final SecurityService securityService;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;

    /**
     * Получить список чат-комнат, в которых состоит указанный пользователь.
     *
     * <p>Использует ChatRoomRepository.findChatRoomsByUserId для фильтрации.
     *
     * @param userId идентификатор пользователя
     * @return отсортированный список активных чат-комнат, в которых участвует пользователь
     */
    @Transactional(readOnly = true)
    public List<ChatRoomDto> getChatRoomsForUser(Integer userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByUserId(userId);

        return chatRooms.stream()
                .map(chatRoom -> convertToDto(chatRoom, userId))
                .collect(Collectors.toList());
    }

    /**
     * Полное преобразование сущности ChatRoom в DTO, включая дополнительную логику.
     */
    private ChatRoomDto convertToDto(ChatRoom chatRoom, Integer currentUserId) {
        // Убедитесь, что в ChatRoomDto добавлены все поля (participants, unreadCount, isOnline, lastSeen)
        ChatRoomDto dto = ChatRoomDto.fromEntity(chatRoom);

        // 1. Получение участника для расчета unreadCount (используем Entity метод)
        ChatParticipant currentParticipant = chatParticipantService.findParticipantEntityByChatAndUser(
                chatRoom.getId(),
                currentUserId
        );

        // 2. Расчет UnreadCount
        if (currentParticipant != null) {
            LocalDateTime lastReadTime = currentParticipant.getLastReadAt();

            Integer unreadCount = messageService.countUnreadMessages(
                    chatRoom.getId(),
                    lastReadTime,
                    currentUserId
            );
            dto.setUnreadCount(unreadCount);
        } else {
            dto.setUnreadCount(0);
        }

        // 3. Получение списка участников (Participants) (теперь с заглушками статуса)
        List<ChatParticipantDto> participants = chatParticipantService.getChatParticipantsByChatId(chatRoom.getId());
        dto.setParticipants(participants);

        // 4. isOnline и lastSeen (логика для 1-на-1 чатов, берем из DTO собеседника)
        if (chatRoom.getType() == ChatRoom.ChatRoomType.PRIVATE && participants.size() == 2) {
            // Находим DTO собеседника
            participants.stream()
                    .filter(p -> !p.getUserId().equals(currentUserId))
                    .findFirst()
                    .ifPresent(otherParticipant -> {
                        // Используем поля-заглушки из ChatParticipantDto
                        dto.setIsOnline(otherParticipant.getIsOnline());
                        dto.setLastSeen(otherParticipant.getLastSeen());
                    });
        }

        return dto;
    }


    /**
     * Создать новую чат-комнату
     *
     * <p>Основной метод для создания чатов в мессенджере. Автоматически устанавливает
     * временные метки и применяет бизнес-правила для разных типов чатов.
     *
     * @param chatRoom данные новой чат-комнаты
     * @return созданная чат-комната
     * @throws IllegalArgumentException если нарушены бизнес-правила создания чата
     */
    public ChatRoom createChatRoom(ChatRoom chatRoom) {
        log.info("Создание новой чат-комнаты: {}", chatRoom.getName());

        // Валидация названия для групповых и публичных чатов
        if ((chatRoom.getType() == ChatRoom.ChatRoomType.GROUP || chatRoom.getType() == ChatRoom.ChatRoomType.PUBLIC)
                && (chatRoom.getName() == null || chatRoom.getName().trim().isEmpty())) {
            throw new IllegalArgumentException("Групповые и публичные чаты должны иметь название");
        }

        // Проверка уникальности названия для активных чатов
        if (chatRoom.getName() != null &&
                chatRoomRepository.existsByNameAndActive(chatRoom.getName())) {
            throw new IllegalArgumentException("Чат с названием '" + chatRoom.getName() + "' уже существует");
        }

        // Установка временных меток
        LocalDateTime now = LocalDateTime.now();
        chatRoom.setCreatedAt(now);
        chatRoom.setUpdatedAt(now);

        // Установка значений по умолчанию в зависимости от типа чата
        if (chatRoom.getMaxParticipants() == null) {
            chatRoom.setMaxParticipants(chatRoom.getType() == ChatRoom.ChatRoomType.PRIVATE ? 2 : 100);
        }

        // Для приватных чатов название может быть null
        if (chatRoom.getType() == ChatRoom.ChatRoomType.PRIVATE) {
            chatRoom.setName(null);
        }

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        log.info("Чат-комната успешно создана с ID: {}", savedChatRoom.getId());

        return savedChatRoom;
    }

    public ChatRoom createOrGetPrivateChat(Integer user1Id, Integer user2Id, User createdBy) {
        log.info("Поиск или создание приватного чата между пользователями {} и {}", user1Id, user2Id);

        // Всегда ищем существующий чат сначала
        Optional<ChatRoom> existingChat = chatRoomRepository.findPrivateChatBetweenUsers(user1Id, user2Id);
        if (existingChat.isPresent()) {
            log.info("Найден существующий приватный чат с ID: {}", existingChat.get().getId());
            return existingChat.get();
        }

        // Создаем новый приватный чат
        ChatRoom privateChat = new ChatRoom();
        privateChat.setType(ChatRoom.ChatRoomType.PRIVATE);
        privateChat.setCreatedBy(createdBy);
        privateChat.setMaxParticipants(2);
        privateChat.setCreatedAt(LocalDateTime.now());
        privateChat.setUpdatedAt(LocalDateTime.now());

        log.info("Создание нового приватного чата для пользователей {} и {}", user1Id, user2Id);
        ChatRoom savedChat = chatRoomRepository.save(privateChat);

        // Автоматически добавляем обоих участников
        createChatParticipant(savedChat, user1Id, "MEMBER");
        createChatParticipant(savedChat, user2Id, "MEMBER");

        log.info("Создан приватный чат ID: {} с участниками {} и {}",
                savedChat.getId(), user1Id, user2Id);

        return savedChat;
    }

    /**
     * Вспомогательный метод для создания участника чата
     */
    private void createChatParticipant(ChatRoom chatRoom, Integer userId, String role) {
        ChatParticipant participant = new ChatParticipant();
        participant.setChatRoom(chatRoom);
        participant.setUser(userRepository.getReferenceById(userId));
        participant.setRole(ChatParticipant.ParticipantRole.valueOf(role));
        participant.setJoinedAt(LocalDateTime.now());
        participant.setIsActive(true);
        participant.setLastReadAt(LocalDateTime.now());

        chatParticipantRepository.save(participant);
        log.debug("Создан участник чата: пользователь {} в чате {}", userId, chatRoom.getId());
    }


    /**
     * Получить все активные чат-комнаты
     *
     * <p>Используется для отображения общего списка доступных чатов в административных интерфейсах
     * или для пользователей с соответствующими правами доступа.
     *
     * @return список активных чат-комнат
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAllActive();
    }

    /**
     * Получить чат-комнаты с сортировкой по времени последнего сообщения
     *
     * <p>Основной метод для построения интерфейса списка чатов в мессенджере.
     * Чаты с самыми свежими сообщениями отображаются вверху.
     *
     * @return отсортированный список активных чат-комнат
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> getChatRoomsOrderedByLastMessage() {
        return chatRoomRepository.findAllActiveOrderByLastMessageDesc();
    }

    /**
     * Получить чат-комнату по ID
     *
     * <p>Основной метод для получения конкретного чата по идентификатору.
     * Используется при переходе в конкретный чат в интерфейсе мессенджера.
     *
     * @param id идентификатор чат-комнаты
     * @return Optional с чат-комнатой или empty если не найдена
     */
    @Transactional(readOnly = true)
    public Optional<ChatRoom> getChatRoomById(Integer id) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(id);
        return chatRoom.filter(cr -> Boolean.TRUE.equals(cr.getIsActive()));
    }

    /**
     * Обновить информацию о чат-комнате
     *
     * <p>Позволяет изменять основные параметры чата: название, описание, аватар и т.д.
     * Автоматически обновляет временную метку изменения.
     *
     * @param id              идентификатор чат-комнаты
     * @param chatRoomDetails новые данные чат-комнаты
     * @return обновленная чат-комната
     * @throws RuntimeException если чат-комната не найдена
     */
    public ChatRoom updateChatRoom(Integer id, ChatRoom chatRoomDetails) {
        log.info("Обновление чат-комнаты с ID: {}", id);

        return chatRoomRepository.findById(id)
                .map(existingChatRoom -> {
                    // Проверяем, что чат активен
                    if (!Boolean.TRUE.equals(existingChatRoom.getIsActive())) {
                        throw new RuntimeException("Нельзя обновить неактивную чат-комнату");
                    }

                    // Обновление только разрешенных полей
                    if (chatRoomDetails.getName() != null) {
                        existingChatRoom.setName(chatRoomDetails.getName());
                    }
                    if (chatRoomDetails.getAvatarUrl() != null) {
                        existingChatRoom.setAvatarUrl(chatRoomDetails.getAvatarUrl());
                    }
                    if (chatRoomDetails.getDescription() != null) {
                        existingChatRoom.setDescription(chatRoomDetails.getDescription());
                    }
                    if (chatRoomDetails.getMaxParticipants() != null) {
                        existingChatRoom.setMaxParticipants(chatRoomDetails.getMaxParticipants());
                    }

                    // Обновление временной метки
                    existingChatRoom.setUpdatedAt(LocalDateTime.now());

                    ChatRoom updatedChatRoom = chatRoomRepository.save(existingChatRoom);
                    log.info("Чат-комната с ID: {} успешно обновлена", id);

                    return updatedChatRoom;
                })
                .orElseThrow(() -> {
                    log.error("Чат-комната с ID: {} не найдена", id);
                    return new RuntimeException("ChatRoom not found with id: " + id);
                });
    }

    /**
     * Удалить чат-комнату (мягкое удаление)
     *
     * <p>Вместо физического удаления из базы данных, помечает чат как неактивный.
     * Это сохраняет историю сообщений и позволяет восстановить чат при необходимости.
     *
     * @param id идентификатор чат-комнаты
     */
    public void deleteChatRoom(Integer id) {
        log.info("Мягкое удаление чат-комнаты с ID: {}", id);

        chatRoomRepository.findById(id).ifPresent(chatRoom -> {
            chatRoom.setIsActive(false);
            chatRoom.setUpdatedAt(LocalDateTime.now());
            chatRoomRepository.save(chatRoom);
            log.info("Чат-комната с ID: {} помечена как неактивная", id);
        });
    }

    /**
     * Обновить информацию о последнем сообщении в чат-комнате.
     * * @param chatRoomId ID чат-комнаты
     * @param lastMessageText Текст последнего сообщения
     * @param senderId ID отправителя последнего сообщения
     */
    @Transactional
    public void updateLastMessageInfo(Integer chatRoomId, String lastMessageText, Integer senderId) {
        // Требуется метод: updateLastMessageInfo в ChatRoomRepository
        chatRoomRepository.updateLastMessageInfo(chatRoomId, lastMessageText, senderId, LocalDateTime.now());
    }

    /**
     * Поиск чат-комнат по названию
     *
     * <p>Реализует функционал поиска чатов в интерфейсе мессенджера.
     * Поиск регистронезависимый и работает по частичному совпадению.
     *
     * @param name часть названия для поиска
     * @return список найденных чат-комнат
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> searchChatRoomsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return chatRoomRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Поиск групповых чатов по названию
     *
     * <p>Специализированный поиск для групповых и публичных чатов.
     * Используется в функционале поиска и присоединения к чатам.
     *
     * @param name часть названия для поиска
     * @return список найденных групповых чатов
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> searchGroupChatsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return chatRoomRepository.searchGroupChatsByName(name);
    }

    /**
     * Получить чат-комнаты с пагинацией
     *
     * <p>Используется для постраничной загрузки списка чатов в интерфейсе
     * при большом количестве чат-комнат в системе.
     *
     * @param pageable параметры пагинации
     * @return страница с чат-комнатами
     */
    @Transactional(readOnly = true)
    public Page<ChatRoom> getChatRooms(Pageable pageable) {
        return chatRoomRepository.findAllActive(pageable);
    }

    /**
     * Получить чат-комнаты по типу
     *
     * <p>Позволяет фильтровать чаты по категориям в интерфейсе мессенджера.
     *
     * @param type тип чат-комнаты
     * @return список чат-комнат указанного типа
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> getChatRoomsByType(ChatRoom.ChatRoomType type) {
        return chatRoomRepository.findByType(type);
    }

    /**
     * Получить количество активных чат-комнат
     *
     * <p>Используется для статистики и мониторинга активности в мессенджере.
     *
     * @return количество активных чат-комнат
     */
    @Transactional(readOnly = true)
    public long getActiveChatRoomsCount() {
        return chatRoomRepository.countActiveChatRooms();
    }
}