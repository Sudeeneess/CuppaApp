package com.cuppa.CuppaApp.repository;

import com.cuppa.CuppaApp.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    Page<Message> findByChatRoomIdOrderBySentAtDesc(Integer chatId, Pageable pageable);
    List<Message> findBySenderId(Integer senderId);
    List<Message> findByChatRoomIdAndMessageType(Integer chatId, Message.MessageType messageType);
    Long countByChatRoomIdAndSenderIdNot(Integer chatRoomId, Integer currentUserId);
    Page<Message> findByChatRoomIdAndContentContainingIgnoreCase(Integer chatRoomId, String content, Pageable pageable);
    List<Message> findByChatRoomIdAndSentAtAfter(Integer chatId, LocalDateTime lastReadAt);

    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatId ORDER BY m.sentAt DESC LIMIT 1")
    Message findLastMessageByChatId(@Param("chatId") Integer chatId);

    Long countByChatRoomId(Integer chatId);

    @Modifying
    @Query("UPDATE Message m SET m.deliveredAt = :deliveredAt WHERE m.chatRoom.id = :chatId AND m.deliveredAt IS NULL AND m.sentAt <= :maxSentAt")
    void markMessagesAsDelivered(@Param("chatId") Integer chatId,
                                 @Param("deliveredAt") LocalDateTime deliveredAt,
                                 @Param("maxSentAt") LocalDateTime maxSentAt);

    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE m.chatRoom.id = :chatRoomId " +
            "AND m.sentAt > :lastReadAt " +
            "AND m.sender.id <> :currentUserId")
    Long countUnreadMessages(@Param("chatRoomId") Integer chatRoomId,
                             @Param("lastReadAt") LocalDateTime lastReadAt,
                             @Param("currentUserId") Integer currentUserId);

    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE m.chatRoom.id = :chatRoomId " +
            "AND m.sentAt > :lastReadTime " +
            "AND m.sender.id != :currentUserId")
    Long countUnreadMessagesAfterTime(@Param("chatRoomId") Integer chatRoomId,
                                      @Param("lastReadTime") LocalDateTime lastReadTime,
                                      @Param("currentUserId") Integer currentUserId);
}