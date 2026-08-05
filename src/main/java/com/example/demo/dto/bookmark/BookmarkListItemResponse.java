package com.example.demo.dto.bookmark;
import com.example.demo.dto.category.CategoryItemResponse;

import java.util.List;


import com.fasterxml.jackson.annotation.JsonProperty;

public class BookmarkListItemResponse {

    @JsonProperty("post_id")
    private final Long postId;

    private final String title;

    @JsonProperty("bookmark_count")
    private final int bookmarkCount;

    @JsonProperty("comment_count")
    private final int commentCount;

    @JsonProperty("view_count")
    private final int viewCount;

    @JsonProperty("created_at")
    private final String createdAt;

    @JsonProperty("bookmarked_at")
    private final String bookmarkedAt;

    @JsonProperty("author_nickname")
    private final String authorNickname;

    @JsonProperty("author_profile_image")
    private final String authorProfileImage;

    private final List<CategoryItemResponse> categories;

    public BookmarkListItemResponse(
            Long postId,
            String title,
            int bookmarkCount,
            int commentCount,
            int viewCount,
            String createdAt,
            String bookmarkedAt,
            String authorNickname,
            String authorProfileImage,
            List<CategoryItemResponse> categories
    ) {
        this.postId = postId;
        this.title = title;
        this.bookmarkCount = bookmarkCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.bookmarkedAt = bookmarkedAt;
        this.authorNickname = authorNickname;
        this.authorProfileImage = authorProfileImage;
        this.categories = categories;
    }

    public Long getPostId() {
        return postId;
    }

    public String getTitle() {
        return title;
    }

    public int getBookmarkCount() {
        return bookmarkCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public int getViewCount() {
        return viewCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getBookmarkedAt() {
        return bookmarkedAt;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public String getAuthorProfileImage() {
        return authorProfileImage;
    }

    public List<CategoryItemResponse> getCategories() {
        return categories;
    }
}