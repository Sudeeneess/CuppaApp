package com.cuppa.CuppaApp.repository;

import com.cuppa.CuppaApp.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с сообщениями в чат-комнатах
 *
 * <p>Обеспечивает доступ к данным сообщений в базе данных, включая
 * поиск, фильтрацию и пагинацию сообщений по различным критериям.
 * Поддерживает сложные запросы для эффективной работы с историей сообщений.
 *
 * @author Petr Panteev
 * @version 1.0
 * @since 15.10.2025
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    /**
     * Поиск сообщений по идентификатору чат-комнаты с пагинацией
     *
     * <p>Возвращает страницу сообщений для указанного чата, отсортированных
     * по времени отправки в порядке убывания (от новых к старым).
     *
     * @param chatId идентификатор чат-комнаты
     * @param pageable параметры пагинации и сортировки
     * @return страница с сообщениями чата
     */
    Page<Message> findByChatRoomIdOrderBySentAtDesc(Integer chatId, Pageable pageable);

    /**
     * Поиск сообщений по идентификатору отправителя
     *
     * <p>Возвращает все сообщения, отправленные указанным пользователем
     * во всех чат-комнатах системы.
     *
     * @param senderId идентификатор отправителя
     * @return список сообщений пользователя
     */
    List<Message> findBySenderId(Integer senderId);

    /**
     * Поиск сообщений по типу в указанной чат-комнате
     *
     * <p>Возвращает сообщения определенного типа (TEXT, IMAGE, FILE и т.д.)
     * в рамках конкретной чат-комнаты.
     *
     * @param chatId идентификатор чат-комнаты
     * @param messageType тип сообщения
     * @return список сообщений указанного типа
     */
    List<Message> findByChatRoomIdAndMessageType(Integer chatId, Message.MessageType messageType);

    /**
     * Поиск непрочитанных сообщений для пользователя в чате
     *
     * <p>Возвращает сообщения, которые были отправлены после указанного времени
     * последнего прочтения пользователем. Используется для подсчета
     * непрочитанных сообщений.
     *
     * @param chatId идентификатор чат-комнаты
     * @param lastReadAt время последнего прочтения пользователем
     * @return список непрочитанных сообщений
     */
    List<Message> findByChatRoomIdAndSentAtAfter(Integer chatId, LocalDateTime lastReadAt);

    /**
     * Поиск сообщений по содержимому (полнотекстовый поиск)
     *
     * <p>Выполняет поиск сообщений, содержащих указанный текст в содержимом.
     * Поддерживает пагинацию для больших объемов данных.
     *
     * @param chatId идентификатор чат-комнаты
     * @param content текст для поиска
     * @param pageable параметры пагинации
     * @return страница с найденными сообщениями
     */
    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :content, '%'))")
    Page<Message> findByChatRoomIdAndContentContainingIgnoreCase(
            @Param("chatId") Integer chatId,
            @Param("content") String content,
            Pageable pageable);

    /**
     * Получение последнего сообщения в чате
     *
     * <p>Возвращает самое последнее сообщение в указанной чат-комнате
     * для отображения в списке чатов.
     *
     * @param chatId идентификатор чат-комнаты
     * @return последнее сообщение или null если сообщений нет
     */
    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatId ORDER BY m.sentAt DESC LIMIT 1")
    Message findLastMessageByChatId(@Param("chatId") Integer chatId);

    /**
     * Подсчет количества сообщений в чате
     *
     * <p>Возвращает общее количество сообщений в указанной чат-комнате.
     * Используется для отображения статистики и информации о чате.
     *
     * @param chatId идентификатор чат-комнаты
     * @return количество сообщений в чате
     */
    Long countByChatRoomId(Integer chatId);

    /**
     * Обновление времени доставки для группы сообщений
     *
     * <p>Устанавливает время доставки для всех сообщений в указанном чате,
     * которые были отправлены до указанного времени и еще не доставлены.
     *
     * @param chatId идентификатор чат-комнаты
     * @param deliveredAt время доставки
     * @param maxSentAt максимальное время отправки для обновления
     */
    @Query("UPDATE Message m SET m.deliveredAt = :deliveredAt WHERE m.chatRoom.id = :chatId AND m.deliveredAt IS NULL AND m.sentAt <= :maxSentAt")
    void markMessagesAsDelivered(@Param("chatId") Integer chatId,
                                 @Param("deliveredAt") LocalDateTime deliveredAt,
                                 @Param("maxSentAt") LocalDateTime maxSentAt);
}