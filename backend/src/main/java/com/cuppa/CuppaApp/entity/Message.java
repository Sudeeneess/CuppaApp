package com.cuppa.CuppaApp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Сущность сообщения (Message) для приложения Cuppa
 *
 * <p>Отображает таблицу "messages" из базы данных в объектную модель Java.
 * Содержит информацию о сообщениях в чат-комнатах системы мессенджера.
 *
 * <p>Поддерживает различные типы сообщений (текст, изображения, файлы),
 * отслеживание статусов доставки и прочтения, а также цепочки ответов.
 *
 * @author Petr Panteev
 * @version 1.0
 * @since 09.10.2025
 */

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_messages_chat_id_sent_at",
                columnList = "chat_id, sent_at DESC")
})
@Data
public class Message {

    /**
     * Уникальный идентификатор сообщения
     *
     * <p>Совпадает с первичным ключом в таблице messages.
     * Является автоинкрементным полем в базе данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * Чат-комната, в которой отправлено сообщение
     *
     * <p>Связь Many-to-One: много сообщений могут быть в одной чат-комнате.
     * Внешний ключ к таблице chat_rooms.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", referencedColumnName = "id")
    private ChatRoom chatRoom;

    /**
     * Отправитель сообщения
     *
     * <p>Связь Many-to-One: один пользователь может отправить много сообщений.
     * Внешний ключ к таблице users.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private User sender;

    /**
     * Содержимое сообщения
     *
     * <p>Текст сообщения или ссылка на медиа-контент.
     * Использует тип TEXT для хранения сообщений любой длины.
     * Не может быть null.
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Тип сообщения
     *
     * <p>Определяет тип содержимого сообщения.
     * По умолчанию устанавливается в TEXT.
     * Возможные значения: TEXT, IMAGE, FILE, VOICE, VIDEO и т.д.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 20)
    private MessageType messageType = MessageType.TEXT;

    /**
     * Дата и время отправки сообщения
     *
     * <p>Устанавливается автоматически при создании сообщения.
     * По умолчанию используется текущая временная метка.
     * Используется для сортировки сообщений в чате.
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * Дата и время доставки сообщения
     *
     * <p>Отслеживает, когда сообщение было доставлено получателям.
     * Может быть null если сообщение еще не доставлено.
     */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /**
     * Дата и время прочтения сообщения
     *
     * <p>Отслеживает, когда сообщение было прочитано получателями.
     * Может быть null если сообщение еще не прочитано.
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Сообщение, на которое данное сообщение является ответом
     *
     * <p>Связь Many-to-One: много сообщений могут отвечать на одно сообщение.
     * Внешний ключ к той же таблице messages.
     * Реализует функциональность "ответ на сообщение".
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id", referencedColumnName = "id")
    private Message replyToMessage;

    /**
     * Флаг редактирования сообщения
     *
     * <p>Показывает, было ли сообщение отредактировано после отправки.
     * По умолчанию устанавливается в false.
     */
    @Column(name = "is_edited")
    private Boolean isEdited = false;

    /**
     * Дата и время последнего редактирования сообщения
     *
     * <p>Отслеживает, когда сообщение было в последний раз отредактировано.
     * Может быть null если сообщение никогда не редактировалось.
     */
    @Column(name = "edited_at")
    private LocalDateTime editedAt;
}

/**
 * Перечисление типов сообщений
 *
 * <p>Определяет возможные типы сообщений в системе.
 * TEXT - текстовое сообщение
 * IMAGE - изображение
 * FILE - файл
 * VOICE - голосовое сообщение
 * VIDEO - видео сообщение
 * SYSTEM - системное сообщение
 */
enum MessageType {
    TEXT,
    IMAGE,
    FILE,
    VOICE,
    VIDEO,
    SYSTEM
}