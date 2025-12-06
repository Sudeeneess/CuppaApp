package com.cuppa.CuppaApp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сущность чат-комнаты (ChatRoom) для приложения Cuppa
 *
 * <p>Отображает таблицу "chat_rooms" из базы данных в объектную модель Java.
 * Содержит основные поля для создания и управления чат-комнатами в системе мессенджера.
 *
 * <p>Поддерживает различные типы чатов (приватные, групповые) и хранит информацию
 * о последнем сообщении для оптимизации отображения списка чатов.
 *
 * @author Petr Panteev
 * @version 1.0
 * @since 09.10.2025
 */

@Table(name = "chat_rooms", indexes = {
        @Index(name = "idx_chat_rooms_last_message_at",
                columnList = "last_message_at DESC")
})
@Entity
@Data
@NoArgsConstructor
public class ChatRoom {

    /**
     * Уникальный идентификатор чат-комнаты
     *
     * <p>Совпадает с первичным ключом в таблице chat_rooms.
     * Является автоинкрементным полем в базе данных с начальным значением 1.
     */
    @Id // Указывает, что поле является первичным ключом
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Сопоставляет с колонкой "id" в таблице
    private Integer id;

    /**
     * Название чат-комнаты
     *
     * <p>Используется для отображения названия чата в интерфейсе.
     * Для приватных чатов может быть null, так как название часто
     * генерируется на основе имен участников.
     */
    @Column(name = "name", length = 255)
    private String name;

    /**
     * Тип чат-комнаты
     *
     * <p>Определяет тип чата: приватный, групповой или публичный.
     * По умолчанию устанавливается в PRIVATE.
     * Хранится в базе данных как строка длиной до 20 символов.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ChatRoomType type = ChatRoomType.PRIVATE;

    /**
     * Создатель чат-комнаты
     *
     * <p>Ссылается на пользователя, который создал данную чат-комнату.
     * Внешний ключ к таблице users.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;

    /**
     * Дата и время создания чат-комнаты
     *
     * <p>Устанавливается автоматически при создании чат-комнаты.
     * Использует текущую временную метку сервера базы данных.
     * Не может быть изменено после создания.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления чат-комнаты
     *
     * <p>Обновляется автоматически при любом изменении данных чат-комнаты.
     * Использует текущую временную метку сервера базы данных.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Дата и время последнего сообщения в чате
     *
     * <p>Используется для сортировки чатов в списке.
     * Обновляется при отправке каждого нового сообщения.
     * Может быть null для новых чатов без сообщений.
     */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /**
     * Текст последнего сообщения в чате
     *
     * <p>Хранит текст последнего сообщения для предпросмотра в списке чатов.
     * Использует тип TEXT для хранения сообщений любой длины.
     * Может быть null для новых чатов без сообщений.
     */
    @Column(name = "last_message_text", columnDefinition = "TEXT")
    private String lastMessageText;

    /**
     * Отправитель последнего сообщения
     *
     * <p>Ссылается на пользователя, который отправил последнее сообщение в чате.
     * Внешний ключ к таблице users.
     * Может быть null для новых чатов без сообщений.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_sender_id", referencedColumnName = "id")
    private User lastMessageSender;

    /**
     * URL аватара чат-комнаты
     *
     * <p>Ссылка на изображение чата (аватар группы).
     * Может быть относительным путем или полным URL.
     * Для приватных чатов часто используется аватар собеседника.
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * Описание чат-комнаты
     *
     * <p>Содержит дополнительную информацию о чате.
     * Часто используется для групповых чатов для описания темы или целей.
     * Использует тип TEXT для хранения описаний любой длины.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Флаг активности чат-комнаты
     *
     * <p>Определяет, является ли чат-комната активной.
     * Неактивные чаты могут быть скрыты в интерфейсе, но сохраняются в базе.
     * По умолчанию устанавливается в true.
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Максимальное количество участников
     *
     * <p>Ограничивает максимальное количество пользователей в чате.
     * Для приватных чатов по умолчанию устанавливается 2 участника.
     * Для групповых чатов может быть увеличено.
     */
    @Column(name = "max_participants")
    private Integer maxParticipants = 2;


    /**
     * Перечисление типов чат-комнат
     *
     * <p>Определяет возможные типы чатов в системе.
     * PRIVATE - приватные чаты между двумя пользователями
     * GROUP - групповые чаты с несколькими участниками
     * PUBLIC - публичные чаты, доступные для всех пользователей
     */
    public static enum ChatRoomType {
        PRIVATE, GROUP, PUBLIC
    }

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY)
    private List<ChatParticipant> participants;
}