package com.example.demo.controller;

import com.example.demo.dto.chat.ChatMessageRequest;
import com.example.demo.dto.chat.ChatMessageResponse;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatMessageController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageController(
            ChatService chatService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/messages")
    public void sendMessage(
            Principal principal,
            @Valid ChatMessageRequest request
    ) {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) principal;

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        ChatMessageResponse response =
                chatService.saveMessage(
                        userDetails.getUserId(),
                        request
                );

        String receiverEmail = chatService.getReceiverEmail(
                request.getChatRoomId(),
                userDetails.getUserId()
        );

        messagingTemplate.convertAndSendToUser(
                receiverEmail,
                "/queue/chat",
                response
        );

        messagingTemplate.convertAndSendToUser(
                userDetails.getEmail(),
                "/queue/chat",
                response
        );
    }
}