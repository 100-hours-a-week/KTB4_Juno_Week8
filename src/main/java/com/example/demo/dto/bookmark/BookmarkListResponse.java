package com.example.demo.dto.bookmark;

import java.util.List;

public class BookmarkListResponse {

    private final List<BookmarkListItemResponse> posts;

    public BookmarkListResponse(List<BookmarkListItemResponse> posts) {
        this.posts = posts;
    }

    public List<BookmarkListItemResponse> getPosts() {
        return posts;
    }
}