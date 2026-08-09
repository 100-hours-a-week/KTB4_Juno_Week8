package com.example.demo.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatMessageRequest {

    @NotNull(message = "채팅방 ID는 필수입니다.")
    private Long chatRoomId;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String content;

    protected ChatMessageRequest() {
    }

    public ChatMessageRequest(
            Long chatRoomId,
            String content
    ) {
        this.chatRoomId = chatRoomId;
        this.content = content;
    }

    public Long getChatRoomId() {
        return chatRoomId;
    }

    public String getContent() {
        return content;
    }
}