package com.example.demo.service;

import com.example.demo.domain.ChatRoom;
import com.example.demo.domain.ChatRoomMember;
import com.example.demo.domain.User;
import com.example.demo.dto.chat.ChatRoomResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.ChatRoomMemberRepository;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    public ChatService(
            ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            UserRepository userRepository,
            ImageService imageService
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
    }

    @Transactional
    public ChatRoomResponse createOrGetChatRoom(
            Long currentUserId,
            Long receiverId
    ) {
        if (currentUserId.equals(receiverId)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "자기 자신과는 채팅방을 생성할 수 없습니다."
            );
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "상대방 사용자를 찾을 수 없습니다."
                ));

        ChatRoom chatRoom = chatRoomMemberRepository
                .findDirectChatRoom(currentUserId, receiverId)
                .orElseGet(() -> createChatRoom(
                        currentUser,
                        receiver
                ));

        String profileImage = imageService.createPresignedUrl(
                receiver.getProfileImage()
        );

        return new ChatRoomResponse(
                chatRoom.getChatRoomId(),
                receiver.getUserId(),
                receiver.getNickname(),
                profileImage
        );
    }

    private ChatRoom createChatRoom(
            User currentUser,
            User receiver
    ) {
        ChatRoom chatRoom = chatRoomRepository.save(
                new ChatRoom()
        );

        chatRoomMemberRepository.save(
                new ChatRoomMember(chatRoom, currentUser)
        );

        chatRoomMemberRepository.save(
                new ChatRoomMember(chatRoom, receiver)
        );

        return chatRoom;
    }
}