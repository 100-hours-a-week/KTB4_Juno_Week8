package com.example.demo.repository;

import com.example.demo.domain.ChatRoom;
import com.example.demo.domain.ChatRoomMember;
import com.example.demo.domain.ChatRoomMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository
        extends JpaRepository<ChatRoomMember, ChatRoomMemberId> {

    boolean existsByChatRoomChatRoomIdAndUserUserId(
            Long chatRoomId,
            Long userId
    );

    List<ChatRoomMember> findAllByChatRoomChatRoomId(
            Long chatRoomId
    );

    @Query("""
            select member1.chatRoom
            from ChatRoomMember member1
            join ChatRoomMember member2
                on member1.chatRoom = member2.chatRoom
            where member1.user.userId = :userId1
              and member2.user.userId = :userId2
              and (
                  select count(member3)
                  from ChatRoomMember member3
                  where member3.chatRoom = member1.chatRoom
              ) = 2
            """)
    Optional<ChatRoom> findDirectChatRoom(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );
}