package com.example.demo.dto.post;

import java.util.List;
import com.example.demo.dto.category.CategoryItemResponse;

public class PostDetailResponse {

    private final Long postId;
    private final String title;
    private final String content;
    private final String image;
    private final int bookmarkCount;
    private final int commentCount;
    private final int viewCount;
    private final boolean bookmarked;
    private final String createdAt;
    private final Long authorId;
    private final String nickname;
    private final String profileImage;
    private final List<PostDetailCommentResponse> comments;
    private final List<CategoryItemResponse> categories;


    public PostDetailResponse(
            Long postId,
            String title,
            String content,
            String image,
            int bookmarkCount,
            int commentCount,
            int viewCount,
            boolean bookmarked,
            String createdAt,
            Long authorId,
            String nickname,
            String profileImage,
            List<CategoryItemResponse> categories,
            List<PostDetailCommentResponse> comments
    ) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.image = image;
        this.bookmarkCount = bookmarkCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.bookmarked = bookmarked;
        this.createdAt = createdAt;
        this.authorId = authorId;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.comments = comments;
        this.categories = categories;
    }

    public Long getPostId() {
        return postId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImage() {
        return image;
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

    public boolean isBookmarked() {
        return bookmarked;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public List<CategoryItemResponse> getCategories() {
        return categories;
    }

    public List<PostDetailCommentResponse> getComments() {
        return comments;
    }
}