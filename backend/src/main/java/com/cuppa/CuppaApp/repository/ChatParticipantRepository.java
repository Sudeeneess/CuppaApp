package com.cuppa.CuppaApp.repository;

import com.cuppa.CuppaApp.entity.ChatParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}