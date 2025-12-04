package com.cuppa.CuppaApp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Сущность участника чата (ChatParticipant) для приложения Cuppa
 *
 * <p>Отображает таблицу "chat_participants" из базы данных в объектную модель Java.
 * Содержит информацию о участниках чат-комнат и их статусах в чатах.
 *
 * <p>Обеспечивает связь многие-ко-многим между пользователями и чат-комнатами
 * с дополнительными атрибутами (роль, время входа, время последнего прочтения).
 *
 * @author Petr Panteev
 * @version 1.0
 * @since 09.10.2025
 */

@Entity
@Table(name = "chat_participants", uniqueConstraints = {
        @UniqueConstraint(name = "chat_participants_chat_id_user_id_key",
                columnNames = {"chat_id", "user_id"})
})
@Data
public class ChatParticipant {

    /**
     * Уникальный идентификатор записи участника чата
     *
     * <p>Совпадает с первичным ключом в таблице chat_participants.
     * Является автоинкрементным полем в базе данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * Чат-комната, в которой состоит участник
     *
     * <p>Связь Many-to-One: много участников могут быть в одной чат-комнате.
     * Внешний ключ к таблице chat_rooms.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", referencedColumnName = "id")
    private ChatRoom chatRoom;

    /**
     * Пользователь-участник чата
     *
     * <p>Связь Many-to-One: один пользователь может быть участником многих чатов.
     * Внешний ключ к таблице users.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    /**
     * Дата и время входа пользователя в чат
     *
     * <p>Устанавливается автоматически при добавлении пользователя в чат.
     * По умолчанию используется текущая временная метка.
     */
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    /**
     * Роль пользователя в чате
     *
     * <p>Определяет права и возможности пользователя в чате.
     * По умолчанию устанавливается в MEMBER.
     * Возможные значения: MEMBER, ADMIN, MODERATOR, OWNER и т.д.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private ParticipantRole role = ParticipantRole.MEMBER;

    /**
     * Флаг активности участника в чате
     *
     * <p>Определяет, является ли участник активным в данном чате.
     * Неактивные участники могут быть скрыты, но сохраняются в базе.
     * По умолчанию устанавливается в true.
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Дата и время последнего прочтения сообщений
     *
     * <p>Используется для отслеживания непрочитанных сообщений.
     * Показывает, до какого времени пользователь прочитал сообщения в чате.
     * Может быть null если пользователь еще не читал сообщения.
     */
    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;


/**
 * Перечисление ролей участников чата
 *
 * <p>Определяет возможные роли пользователей в чат-комнатах.
 * MEMBER - обычный участник с базовыми правами
 * ADMIN - администратор с расширенными правами управления
 * MODERATOR - модератор с правами управления сообщениями
 * OWNER - владелец чата с полными правами
 */
public static enum ParticipantRole {
    MEMBER,
    ADMIN,
    MODERATOR,
    OWNER
}
}