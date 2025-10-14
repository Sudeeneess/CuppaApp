package com.cuppa.CuppaApp.repository;

import com.cuppa.CuppaApp.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с чат-комнатами в веб-мессенджере Cuppa
 *
 * <p>Предоставляет методы для доступа к данным чат-комнат, включая поиск,
 * фильтрацию и обновление информации о чатах. Оптимизирован для работы
 * с большими объемами данных в реальном времени мессенджера.
 *
 * @author Petr Panteev
 * @version 1.0
 * @since 14.10.2025
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {

    /**
     * Найти все активные чат-комнаты
     *
     * <p>Используется для отображения списка доступных чатов в интерфейсе мессенджера.
     * Возвращает только активные чаты.
     *
     * @return список активных чат-комнат
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isActive = true")
    List<ChatRoom> findAllActive();

    /**
     * Найти все активные чат-комнаты отсортированные по времени последнего сообщения
     *
     * <p>Используется для отображения списка доступных чатов в интерфейсе мессенджера.
     * Возвращает только активные чаты, отсортированные по убыванию времени последнего сообщения.
     *
     * @return список активных чат-комнат, отсортированный по времени последнего сообщения
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isActive = true ORDER BY cr.lastMessageAt DESC")
    List<ChatRoom> findAllActiveOrderByLastMessageDesc();

    /**
     * Найти чат-комнаты по типу
     *
     * <p>Позволяет фильтровать чаты по типу (приватные, групповые, публичные).
     * Используется для отображения разных категорий чатов в интерфейсе мессенджера.
     *
     * @param type тип чат-комнаты
     * @return список чат-комнат указанного типа
     */
    List<ChatRoom> findByType(ChatRoom.ChatRoomType type);

    /**
     * Найти чат-комнаты по создателю
     *
     * <p>Используется для отображения чатов, созданных конкретным пользователем.
     * Полезно для функционала управления созданными чатами в мессенджере.
     *
     * @param createdById идентификатор пользователя-создателя
     * @return список чат-комнат, созданных пользователем
     */
    List<ChatRoom> findByCreatedById(Integer createdById);

    /**
     * Найти чат-комнаты по отправителю последнего сообщения
     *
     * <p>Позволяет найти чаты, в которых пользователь недавно отправлял сообщения.
     * Используется для быстрого доступа к активным диалогам в мессенджере.
     *
     * @param lastMessageSenderId идентификатор отправителя
     * @return список чат-комнат с последними сообщениями от указанного пользователя
     */
    List<ChatRoom> findByLastMessageSenderId(Integer lastMessageSenderId);

    /**
     * Найти чат-комнаты с последними сообщениями после указанной даты
     *
     * <p>Используется для получения списка недавно активных чатов.
     * Оптимизирует отображение "свежих" диалогов в интерфейсе мессенджера.
     *
     * @param date пороговая дата
     * @return список чат-комнат с сообщениями после указанной даты
     */
    List<ChatRoom> findByLastMessageAtAfter(LocalDateTime date);

    /**
     * Поиск чат-комнат по названию (регистронезависимый)
     *
     * <p>Реализует функционал поиска чатов по имени в интерфейсе мессенджера.
     * Игнорирует регистр для удобства пользователей.
     *
     * @param name часть названия для поиска
     * @return список найденных чат-комнат
     */
    List<ChatRoom> findByNameContainingIgnoreCase(String name);

    /**
     * Найти активные чат-комнаты с пагинацией
     *
     * <p>Обеспечивает постраничную загрузку списка чатов для оптимизации производительности
     * при большом количестве чат-комнат в мессенджере.
     *
     * @param pageable параметры пагинации
     * @return страница с чат-комнатами
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isActive = true")
    Page<ChatRoom> findAllActive(Pageable pageable);

    /**
     * Найти приватные чат-комнаты между двумя пользователями
     *
     * <p>Специальный метод для поиска существующих приватных чатов между пользователями.
     * Используется для предотвращения создания дубликатов приватных чатов в мессенджере.
     *
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @return Optional с найденной чат-комнатой или empty если не найдена
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.type = 'PRIVATE' " +
            "AND EXISTS (SELECT 1 FROM ChatParticipant cp1 WHERE cp1.chatRoom = cr AND cp1.user.id = :user1Id) " +
            "AND EXISTS (SELECT 1 FROM ChatParticipant cp2 WHERE cp2.chatRoom = cr AND cp2.user.id = :user2Id)")
    Optional<ChatRoom> findPrivateChatBetweenUsers(@Param("user1Id") Integer user1Id, @Param("user2Id") Integer user2Id);

    /**
     * Обновить информацию о последнем сообщении в чате
     *
     * <p>Критически важный метод для мессенджера - обновляет превью чата при отправке нового сообщения.
     * Устанавливает текст, отправителя и время последнего сообщения для отображения в списке чатов.
     *
     * @param chatRoomId идентификатор чат-комнаты
     * @param lastMessageText текст последнего сообщения
     * @param lastMessageSenderId идентификатор отправителя
     * @param lastMessageAt время отправки сообщения
     */
    @Modifying
    @Query("UPDATE ChatRoom cr SET cr.lastMessageText = :lastMessageText, " +
            "cr.lastMessageSender.id = :lastMessageSenderId, " +
            "cr.lastMessageAt = :lastMessageAt, " +
            "cr.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE cr.id = :chatRoomId")
    void updateLastMessageInfo(@Param("chatRoomId") Integer chatRoomId,
                               @Param("lastMessageText") String lastMessageText,
                               @Param("lastMessageSenderId") Integer lastMessageSenderId,
                               @Param("lastMessageAt") LocalDateTime lastMessageAt);

    /**
     * Поиск групповых чатов по названию
     *
     * <p>Специализированный поиск для групповых чатов, исключая приватные диалоги.
     * Используется в функционале поиска и присоединения к публичным чатам в мессенджере.
     *
     * @param name часть названия для поиска
     * @return список найденных групповых чатов
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.type IN ('GROUP', 'PUBLIC') " +
            "AND LOWER(cr.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "AND cr.isActive = true")
    List<ChatRoom> searchGroupChatsByName(@Param("name") String name);

    /**
     * Проверить существование чат-комнаты с указанным названием
     *
     * <p>Используется для валидации при создании новых чатов, чтобы избежать дубликатов названий
     * в системе мессенджера.
     *
     * @param name название для проверки
     * @return true если чат с таким названием существует
     */
    @Query("SELECT COUNT(cr) > 0 FROM ChatRoom cr WHERE cr.name = :name AND cr.isActive = true")
    boolean existsByNameAndActive(@Param("name") String name);

    /**
     * Получить количество активных чат-комнат
     *
     * <p>Используется для статистики и мониторинга активности в мессенджере.
     *
     * @return количество активных чат-комнат
     */
    @Query("SELECT COUNT(cr) FROM ChatRoom cr WHERE cr.isActive = true")
    long countActiveChatRooms();
}
