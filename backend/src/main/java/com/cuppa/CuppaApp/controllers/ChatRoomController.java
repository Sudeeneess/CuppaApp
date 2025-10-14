package com.cuppa.CuppaApp.controllers;

import com.cuppa.CuppaApp.entity.ChatRoom;
import com.cuppa.CuppaApp.entity.User;
import com.cuppa.CuppaApp.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для управления чат-комнатами в веб-мессенджере Cuppa
 *
 * <p>Предоставляет API endpoints для создания, поиска и управления чат-комнатами.
 * Все методы возвращают стандартизированные HTTP ответы и логируют операции.
 *
 * @author Petr Panteev
 * @version 1.0
 * @since 14.10.2025
 */
@RestController
@RequestMapping("/api/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * Получить список всех активных чат-комнат
     *
     * <p>Используется для загрузки общего списка чатов в интерфейсе мессенджера.
     * Возвращает только активные чаты, отсортированные по времени последнего сообщения.
     *
     * @return ResponseEntity со списком активных чат-комнат
     */
    @GetMapping
    public ResponseEntity<List<ChatRoom>> getAllChatRooms() {
        try {
            List<ChatRoom> chatRooms = chatRoomService.getChatRoomsOrderedByLastMessage();
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить чат-комнату по ID
     *
     * <p>Основной endpoint для получения информации о конкретном чате.
     * Используется при переходе пользователя в конкретный диалог.
     *
     * @param id идентификатор чат-комнаты
     * @return ResponseEntity с чат-комнатой или 404 если не найдена
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChatRoom> getChatRoomById(@PathVariable Integer id) {
        try {
            return chatRoomService.getChatRoomById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Создать новую чат-комнату
     *
     * <p>Endpoint для создания новых чатов в мессенджере. Поддерживает создание
     * приватных, групповых и публичных чатов с соответствующей валидацией.
     *
     * @param chatRoom данные новой чат-комнаты
     * @return ResponseEntity с созданной чат-комнатой и статусом 201
     */
    @PostMapping
    public ResponseEntity<?> createChatRoom(@RequestBody ChatRoom chatRoom) {
        try {
            ChatRoom createdChatRoom = chatRoomService.createChatRoom(chatRoom);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdChatRoom);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при создании чат-комнаты");
        }
    }

    /**
     * Создать или получить приватный чат между пользователями
     *
     * <p>Специальный endpoint для создания приватных диалогов. Предотвращает
     * создание дубликатов чатов между одними и теми же пользователями.
     *
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @param createdBy идентификатор пользователя, создающего чат
     * @return ResponseEntity с приватным чатом
     */
    @PostMapping("/private")
    public ResponseEntity<ChatRoom> createOrGetPrivateChat(
            @RequestParam Integer user1Id,
            @RequestParam Integer user2Id,
            @RequestParam Integer createdBy) {
        try {
            // В реальном приложении здесь был бы объект User
            User creator = new User();
            creator.setId(createdBy);

            ChatRoom privateChat = chatRoomService.createOrGetPrivateChat(user1Id, user2Id, creator);
            return ResponseEntity.status(HttpStatus.CREATED).body(privateChat);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Обновить информацию о чат-комнате
     *
     * <p>Позволяет изменять основные параметры чата: название, описание, аватар.
     * Используется в настройках чата в интерфейсе мессенджера.
     *
     * @param id идентификатор чат-комнаты
     * @param chatRoomDetails новые данные чат-комнаты
     * @return ResponseEntity с обновленной чат-комнатой
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateChatRoom(@PathVariable Integer id, @RequestBody ChatRoom chatRoomDetails) {
        try {
            ChatRoom updatedChatRoom = chatRoomService.updateChatRoom(id, chatRoomDetails);
            return ResponseEntity.ok(updatedChatRoom);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при обновлении чат-комнаты");
        }
    }

    /**
     * Удалить чат-комнату (мягкое удаление)
     *
     * <p>Помечает чат как неактивный вместо физического удаления.
     * Сохраняет историю сообщений для возможного восстановления.
     *
     * @param id идентификатор чат-комнаты
     * @return ResponseEntity с статусом 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable Integer id) {
        try {
            chatRoomService.deleteChatRoom(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Поиск чат-комнат по названию
     *
     * <p>Endpoint для функционала поиска чатов в интерфейсе мессенджера.
     * Поддерживает регистронезависимый поиск по частичному совпадению.
     *
     * @param name часть названия для поиска
     * @return ResponseEntity со списком найденных чат-комнат
     */
    @GetMapping("/search")
    public ResponseEntity<List<ChatRoom>> searchChatRoomsByName(@RequestParam String name) {
        try {
            List<ChatRoom> chatRooms = chatRoomService.searchChatRoomsByName(name);
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Поиск групповых чатов по названию
     *
     * <p>Специализированный поиск для групповых и публичных чатов.
     * Используется в функционале поиска и присоединения к чатам.
     *
     * @param name часть названия для поиска
     * @return ResponseEntity со списком найденных групповых чатов
     */
    @GetMapping("/search/group")
    public ResponseEntity<List<ChatRoom>> searchGroupChatsByName(@RequestParam String name) {
        try {
            List<ChatRoom> chatRooms = chatRoomService.searchGroupChatsByName(name);
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить чат-комнаты по типу
     *
     * <p>Позволяет фильтровать чаты по категориям в интерфейсе мессенджера.
     *
     * @param type тип чат-комнаты
     * @return ResponseEntity со списком чат-комнат указанного типа
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ChatRoom>> getChatRoomsByType(@PathVariable ChatRoom.ChatRoomType type) {
        try {
            List<ChatRoom> chatRooms = chatRoomService.getChatRoomsByType(type);
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить чат-комнаты с пагинацией
     *
     * <p>Endpoint для постраничной загрузки списка чатов в интерфейсе
     * при большом количестве чат-комнат в системе.
     *
     * @param page номер страницы (по умолчанию 0)
     * @param size размер страницы (по умолчанию 20)
     * @return ResponseEntity со страницей чат-комнат
     */
    @GetMapping("/paged")
    public ResponseEntity<Page<ChatRoom>> getChatRoomsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("lastMessageAt").descending());
            Page<ChatRoom> chatRooms = chatRoomService.getChatRooms(pageable);
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Обновить информацию о последнем сообщении
     *
     * <p>Служебный endpoint, вызываемый при отправке нового сообщения в чат.
     * Обновляет превью чата в списке диалогов для всех участников.
     *
     * @param chatRoomId идентификатор чат-комнаты
     * @param lastMessageText текст последнего сообщения
     * @param senderId идентификатор отправителя
     * @return ResponseEntity с статусом 204 No Content
     */
    @PatchMapping("/{chatRoomId}/last-message")
    public ResponseEntity<Void> updateLastMessage(
            @PathVariable Integer chatRoomId,
            @RequestParam String lastMessageText,
            @RequestParam Integer senderId) {
        try {
            chatRoomService.updateLastMessageInfo(chatRoomId, lastMessageText, senderId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}