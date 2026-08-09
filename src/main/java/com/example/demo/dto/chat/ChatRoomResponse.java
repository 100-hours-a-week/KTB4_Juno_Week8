package com.example.demo.dto.chat;

public class ChatRoomResponse {

    private final Long chatRoomId;
    private final Long userId;
    private final String nickname;
    private final String profileImage;

    public ChatRoomResponse(
            Long chatRoomId,
            Long userId,
            String nickname,
            String profileImage
    ) {
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    public Long getChatRoomId() {
        return chatRoomId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImage() {
        return profileImage;
    }
}