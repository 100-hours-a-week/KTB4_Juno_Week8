package com.example.demo.dto.chat;

import jakarta.validation.constraints.NotNull;

public class ChatRoomCreateRequest {

    @NotNull(message = "상대방 사용자 ID는 필수입니다.")
    private Long receiverId;

    protected ChatRoomCreateRequest() {
    }

    public ChatRoomCreateRequest(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Long getReceiverId() {
        return receiverId;
    }
}