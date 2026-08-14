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
import org.springframework.transaction.annotation.Transactional;import com.example.demo.domain.ChatMessage;
import com.example.demo.dto.chat.ChatMessageRequest;
import com.example.demo.dto.chat.ChatMessageResponse;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.domain.ChatRoomMember;

import java.util.List;
import com.example.demo.dto.chat.ChatMessageListResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;


@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    public ChatService(
            ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            ChatMessageRepository chatMessageRepository,
            UserRepository userRepository,
            ImageService imageService
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.chatMessageRepository = chatMessageRepository;
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
    @Transactional
    public ChatMessageResponse saveMessage(
            Long senderId,
            ChatMessageRequest request
    ) {
        ChatRoom chatRoom = chatRoomRepository
                .findById(request.getChatRoomId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "채팅방을 찾을 수 없습니다."
                ));

        boolean isMember =
                chatRoomMemberRepository
                        .existsByChatRoomChatRoomIdAndUserUserId(
                                chatRoom.getChatRoomId(),
                                senderId
                        );

        if (!isMember) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "해당 채팅방에 참여할 권한이 없습니다."
            );
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));

        ChatMessage chatMessage = chatMessageRepository.save(
                new ChatMessage(
                        chatRoom,
                        sender,
                        request.getContent().trim()
                )
        );

        return new ChatMessageResponse(
                chatMessage.getMessageId(),
                chatRoom.getChatRoomId(),
                sender.getUserId(),
                sender.getNickname(),
                chatMessage.getContent(),
                chatMessage.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public String getReceiverEmail(
            Long chatRoomId,
            Long senderId
    ) {
        List<ChatRoomMember> members =
                chatRoomMemberRepository
                        .findAllByChatRoomChatRoomId(chatRoomId);

        User receiver = members.stream()
                .map(ChatRoomMember::getUser)
                .filter(user -> !user.getUserId().equals(senderId))
                .findFirst()
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "채팅 상대방을 찾을 수 없습니다."
                ));

        return receiver.getEmail();
    }
    @Transactional(readOnly = true)
    public ChatMessageListResponse getMessages(
            Long currentUserId,
            Long chatRoomId,
            Long cursor,
            int size
    ) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "채팅방을 찾을 수 없습니다."
                ));

        boolean isMember =
                chatRoomMemberRepository
                        .existsByChatRoomChatRoomIdAndUserUserId(
                                chatRoomId,
                                currentUserId
                        );

        if (!isMember) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "해당 채팅방의 메시지를 조회할 권한이 없습니다."
            );
        }

        int requestSize = size + 1;
        Pageable pageable = PageRequest.of(0, requestSize);

        List<ChatMessage> chatMessages;

        if (cursor == null) {
            chatMessages =
                    chatMessageRepository
                            .findByChatRoomChatRoomIdOrderByMessageIdDesc(
                                    chatRoom.getChatRoomId(),
                                    pageable
                            );
        } else {
            chatMessages =
                    chatMessageRepository
                            .findByChatRoomChatRoomIdAndMessageIdLessThanOrderByMessageIdDesc(
                                    chatRoom.getChatRoomId(),
                                    cursor,
                                    pageable
                            );
        }

        boolean hasNext = chatMessages.size() > size;

        if (hasNext) {
            chatMessages = new ArrayList<>(
                    chatMessages.subList(0, size)
            );
        } else {
            chatMessages = new ArrayList<>(chatMessages);
        }

        Collections.reverse(chatMessages);

        List<ChatMessageResponse> messages = chatMessages.stream()
                .map(message -> new ChatMessageResponse(
                        message.getMessageId(),
                        message.getChatRoom().getChatRoomId(),
                        message.getSender().getUserId(),
                        message.getSender().getNickname(),
                        message.getContent(),
                        message.getCreatedAt()
                ))
                .toList();

        Long nextCursor = messages.isEmpty()
                ? null
                : messages.get(0).getMessageId();

        return new ChatMessageListResponse(
                messages,
                nextCursor,
                hasNext
        );
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessagesAfter(
            Long currentUserId,
            Long chatRoomId,
            Long lastMessageId
    ) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "채팅방을 찾을 수 없습니다."
                ));

        boolean isMember =
                chatRoomMemberRepository
                        .existsByChatRoomChatRoomIdAndUserUserId(
                                chatRoomId,
                                currentUserId
                        );

        if (!isMember) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "해당 채팅방의 메시지를 조회할 권한이 없습니다."
            );
        }

        List<ChatMessage> chatMessages =
                chatMessageRepository
                        .findByChatRoomChatRoomIdAndMessageIdGreaterThanOrderByMessageIdAsc(
                                chatRoom.getChatRoomId(),
                                lastMessageId
                        );

        return chatMessages.stream()
                .map(message -> new ChatMessageResponse(
                        message.getMessageId(),
                        message.getChatRoom().getChatRoomId(),
                        message.getSender().getUserId(),
                        message.getSender().getNickname(),
                        message.getContent(),
                        message.getCreatedAt()
                ))
                .toList();
    }
}