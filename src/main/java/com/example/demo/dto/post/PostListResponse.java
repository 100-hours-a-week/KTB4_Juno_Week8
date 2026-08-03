package com.example.demo.dto.post;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PostListResponse {

    private List<PostListItemResponse> posts;

    private int page;

    private int size;

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("total_elements")
    private long totalElements;

    private boolean first;

    private boolean last;

    @JsonProperty("has_next")
    private boolean hasNext;

    @JsonProperty("has_previous")
    private boolean hasPrevious;

    public PostListResponse(
            List<PostListItemResponse> posts,
            int page,
            int size,
            int totalPages,
            long totalElements,
            boolean first,
            boolean last,
            boolean hasNext,
            boolean hasPrevious
    ) {
        this.posts = posts;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.first = first;
        this.last = last;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    public List<PostListItemResponse> getPosts() {
        return posts;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }
}