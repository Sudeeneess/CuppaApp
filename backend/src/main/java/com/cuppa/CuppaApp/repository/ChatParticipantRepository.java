package com.cuppa.CuppaApp.repository;

import com.cuppa.CuppaApp.entity.ChatParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Integer> {

    List<ChatParticipant> findByChatRoomId(Integer chatId);

    Page<ChatParticipant> findByChatRoomIdAndIsActiveTrue(Integer chatId, Pageable pageable);

    Optional<ChatParticipant> findByChatRoomIdAndUserId(Integer chatId, Integer userId);

    List<ChatParticipant> findByUserIdAndIsActiveTrue(Integer userId);

    boolean existsByChatRoomIdAndUserIdAndIsActiveTrue(Integer chatId, Integer userId);

    /**
     * Проверяет, существует ли участник чата по идентификатору чата и email пользователя
     *
     * @param chatRoomId идентификатор чат-комнаты
     * @param userEmail  email пользователя
     * @return true если пользователь является участником чата, иначе false
     * @author Walerya Pleskova
     * @since 2025-10-28
     */
    @Query("SELECT COUNT(cp) > 0 FROM ChatParticipant cp " +
            "JOIN cp.user u " +
            "WHERE cp.chatRoom.id = :chatRoomId AND u.email = :userEmail AND cp.isActive = true")
    boolean existsByChatRoomIdAndUserEmail(@Param("chatRoomId") Integer chatRoomId,
                                           @Param("userEmail") String userEmail);

}