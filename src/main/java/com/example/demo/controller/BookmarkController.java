package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.bookmark.BookmarkListResponse;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/bookmarks")
public class BookmarkController {

    private final PostService postService;

    public BookmarkController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<BookmarkListResponse>> getBookmarkList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        BookmarkListResponse response =
                postService.getBookmarkList(userDetails.getUserId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "북마크 게시글 목록 조회에 성공하였습니다.",
                        response
                ));
    }
}