package com.example.demo.dto.chat;

import java.time.LocalDateTime;

public class ChatMessageResponse {

    private final Long messageId;
    private final Long chatRoomId;
    private final Long senderId;
    private final String senderNickname;
    private final String content;
    private final LocalDateTime createdAt;

    public ChatMessageResponse(
            Long messageId,
            Long chatRoomId,
            Long senderId,
            String senderNickname,
            String content,
            LocalDateTime createdAt
    ) {
        this.messageId = messageId;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getMessageId() {
        return messageId;
    }

    public Long getChatRoomId() {
        return chatRoomId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}