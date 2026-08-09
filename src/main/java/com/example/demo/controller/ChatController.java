package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.chat.ChatRoomCreateRequest;
import com.example.demo.dto.chat.ChatRoomResponse;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.dto.chat.ChatMessageListResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/chat/rooms")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createOrGetChatRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChatRoomCreateRequest request
    ) {
        ChatRoomResponse response =
                chatService.createOrGetChatRoom(
                        userDetails.getUserId(),
                        request.getReceiverId()
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "채팅방 조회 또는 생성에 성공하였습니다.",
                        response
                ));
    }
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageListResponse>> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "30") int size
    ) {
        ChatMessageListResponse response =
                chatService.getMessages(
                        userDetails.getUserId(),
                        chatRoomId,
                        cursor,
                        size
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "채팅 메시지 조회에 성공하였습니다.",
                        response
                ));
    }
}