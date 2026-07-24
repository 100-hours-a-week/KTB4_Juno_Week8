package com.example.demo.dto.bookmark;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostBookmarkResponse {

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("bookmark_count")
    private int bookmarkCount;

    private boolean bookmarked;

    public PostBookmarkResponse(
            Long postId,
            int bookmarkCount,
            boolean bookmarked
    ) {
        this.postId = postId;
        this.bookmarkCount = bookmarkCount;
        this.bookmarked = bookmarked;
    }

    public Long getPostId() {
        return postId;
    }

    public int getBookmarkCount() {
        return bookmarkCount;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }
}