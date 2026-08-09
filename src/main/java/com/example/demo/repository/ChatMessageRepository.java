package com.example.demo.repository;

import com.example.demo.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomChatRoomIdOrderByMessageIdDesc(
            Long chatRoomId,
            Pageable pageable
    );

    List<ChatMessage> findByChatRoomChatRoomIdAndMessageIdLessThanOrderByMessageIdDesc(
            Long chatRoomId,
            Long messageId,
            Pageable pageable
    );

    List<ChatMessage> findByChatRoomChatRoomIdAndMessageIdGreaterThanOrderByMessageIdAsc(
            Long chatRoomId,
            Long messageId
    );
}