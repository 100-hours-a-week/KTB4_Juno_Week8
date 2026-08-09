package com.example.demo.dto.chat;

import java.util.List;

public class ChatMessageListResponse {

    private final List<ChatMessageResponse> messages;
    private final Long nextCursor;
    private final boolean hasNext;

    public ChatMessageListResponse(
            List<ChatMessageResponse> messages,
            Long nextCursor,
            boolean hasNext
    ) {
        this.messages = messages;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }

    public List<ChatMessageResponse> getMessages() {
        return messages;
    }

    public Long getNextCursor() {
        return nextCursor;
    }

    public boolean isHasNext() {
        return hasNext;
    }
}