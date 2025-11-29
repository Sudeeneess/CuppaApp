package com.cuppa.CuppaApp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность сообщения (Message) для приложения Cuppa
 *
 * <p>Отображает таблицу "messages" из базы данных в объектную модель Java.
 * Содержит информацию о тексте, отправителе, времени и статусах сообщения.</p>
 *
 * @author Petr Panteev
 * @version 1.1
 * @since 15.10.2025
 */
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // Связь с чат-комнатой
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatRoom chatRoom;

    // Связь с отправителем
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /** Содержимое сообщения. */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /** Тип сообщения (TEXT, IMAGE, FILE и т.д.) */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 20)
    private MessageType messageType = MessageType.TEXT;

    /** Время отправки сообщения. Соответствует sent_at в БД. */
    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();

    /** * Время доставки сообщения получателю. Соответствует delivered_at в БД.
     * Используется для статуса "доставлено" (одна галочка).
     */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /** * Время прочтения сообщения получателем. Соответствует read_at в БД.
     * Используется для статуса "прочитано" (две синих галочки).
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /** Ссылка на сообщение, на которое был дан ответ (если есть) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id")
    private Message replyToMessage;

    /** Флаг, указывающий, было ли сообщение отредактировано. */
    @Column(name = "is_edited")
    private Boolean isEdited = false;

    /** * Время последнего редактирования сообщения. Соответствует edited_at в БД.
     * Это поле, которое в коде мы ранее ошибочно называли updatedAt.
     */
    @Column(name = "edited_at")
    private LocalDateTime editedAt;


    /** Перечисление для типов сообщений */
    public enum MessageType {
        TEXT, IMAGE, FILE, VOICE, VIDEO, SYSTEM
    }
}