package com.example.demo.dto.post;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostListItemResponse {

    @JsonProperty("post_id")
    private Long postId;

    private String title;

    @JsonProperty("bookmark_count")
    private int bookmarkCount;

    @JsonProperty("comment_count")
    private int commentCount;

    @JsonProperty("view_count")
    private int viewCount;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("author_nickname")
    private String authorNickname;

    @JsonProperty("author_profile_image")
    private String authorProfileImage;

    public PostListItemResponse(
            Long postId,
            String title,
            int bookmarkCount,
            int commentCount,
            int viewCount,
            String createdAt,
            String authorNickname,
            String authorProfileImage
    ) {
        this.postId = postId;
        this.title = title;
        this.bookmarkCount = bookmarkCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.authorNickname = authorNickname;
        this.authorProfileImage = authorProfileImage;
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

    public String getAuthorNickname() {
        return authorNickname;
    }

    public String getAuthorProfileImage() {
        return authorProfileImage;
    }
}