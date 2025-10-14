package com.cuppa.CuppaApp.service;

import com.cuppa.CuppaApp.entity.ChatRoom;
import com.cuppa.CuppaApp.entity.User;
import com.cuppa.CuppaApp.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    /**
     * Создать или найти приватный чат между двумя пользователями
     *
     * <p>Специальный метод для мессенджера, который предотвращает создание дубликатов
     * приватных чатов между одними и теми же пользователями.
     *
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @param createdBy пользователь, инициирующий создание чата
     * @return существующий или новый приватный чат
     */
    public ChatRoom createOrGetPrivateChat(Integer user1Id, Integer user2Id, User createdBy) {
        log.info("Поиск или создание приватного чата между пользователями {} и {}", user1Id, user2Id);

        // Поиск существующего приватного чата
        Optional<ChatRoom> existingChat = chatRoomRepository.findPrivateChatBetweenUsers(user1Id, user2Id);
        if (existingChat.isPresent()) {
            log.info("Найден существующий приватный чат с ID: {}", existingChat.get().getId());
            return existingChat.get();
        }

        // Создание нового приватного чата
        ChatRoom privateChat = new ChatRoom();
        privateChat.setType(ChatRoom.ChatRoomType.PRIVATE);
        privateChat.setCreatedBy(createdBy);
        privateChat.setMaxParticipants(2);

        log.info("Создание нового приватного чата для пользователей {} и {}", user1Id, user2Id);
        return createChatRoom(privateChat);
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
     * @param id идентификатор чат-комнаты
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
     * Обновить информацию о последнем сообщении в чате
     *
     * <p>Критически важный метод для мессенджера, вызывается при каждой отправке сообщения.
     * Обновляет превью чата в списке диалогов для всех участников.
     *
     * @param chatRoomId идентификатор чат-комнаты
     * @param lastMessageText текст последнего сообщения
     * @param senderId идентификатор отправителя
     */
    public void updateLastMessageInfo(Integer chatRoomId, String lastMessageText, Integer senderId) {
        log.debug("Обновление информации о последнем сообщении в чате: {}", chatRoomId);

        LocalDateTime now = LocalDateTime.now();
        chatRoomRepository.updateLastMessageInfo(chatRoomId, lastMessageText, senderId, now);

        log.debug("Информация о последнем сообщении обновлена для чата: {}", chatRoomId);
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
            return List.of();
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
            return List.of();
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