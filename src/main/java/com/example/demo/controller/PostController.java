package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.comment.*;
import com.example.demo.dto.bookmark.PostBookmarkResponse;
import com.example.demo.dto.post.*;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreatePostResponse>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatePostRequest request
    ) {
        CreatePostResponse response = postService.createPost(
                userDetails.getUserId(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("게시글 작성에 성공하였습니다.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PostListResponse>> getPostList() {
        PostListResponse response = postService.getPostList();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("게시글 목록 조회에 성공하였습니다.", response));
    }

    @GetMapping("/{post_id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("post_id") Long postId
    ) {
        PostDetailResponse response = postService.getPost(
                userDetails.getUserId(),
                postId
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("게시글 상세 조회에 성공하였습니다.", response));
    }

    @PatchMapping("/{post_id}")
    public ResponseEntity<ApiResponse<UpdatePostResponse>> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("post_id") Long postId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        UpdatePostResponse response = postService.updatePost(
                userDetails.getUserId(),
                postId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("게시글 수정에 성공하였습니다.", response));
    }

    @DeleteMapping("/{post_id}")
    public ResponseEntity<ApiResponse<DeletePostResponse>> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("post_id") Long postId
    ) {
        DeletePostResponse response = postService.deletePost(
                userDetails.getUserId(),
                postId
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("게시글 삭제에 성공하였습니다.", response));
    }

    @PostMapping("/{post_id}/comments")
    public ResponseEntity<ApiResponse<CreateCommentResponse>> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("post_id") Long postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CreateCommentResponse response = postService.createComment(
                userDetails.getUserId(),
                postId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("댓글 작성에 성공하였습니다.", response));
    }

    @PatchMapping("/{post_id}/comments/{comment_id}")
    public ResponseEntity<ApiResponse<UpdateCommentResponse>> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("post_id") Long postId,
            @PathVariable("comment_id") Long commentId,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        UpdateCommentResponse response = postService.updateComment(
                userDetails.getUserId(),
                postId,
                commentId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("댓글 수정에 성공하였습니다.", response));
    }

    @DeleteMapping("/{post_id}/comments/{comment_id}")
    public ResponseEntity<ApiResponse<DeleteCommentResponse>> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("post_id") Long postId,
            @PathVariable("comment_id") Long commentId
    ) {
        DeleteCommentResponse response = postService.deleteComment(
                userDetails.getUserId(),
                postId,
                commentId
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("댓글 삭제에 성공하였습니다.", response));
    }

    @PostMapping("/{post_id}/bookmarks")
    public ResponseEntity<ApiResponse<PostBookmarkResponse>> createBookmark(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("post_id") Long postId
    ) {
        PostBookmarkResponse response = postService.createBookmark(
                userDetails.getUserId(),
                postId
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("북마크가 추가되었습니다.", response));
    }

    @DeleteMapping("/{post_id}/bookmarks")
    public ResponseEntity<ApiResponse<PostBookmarkResponse>> deleteBookmark(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("post_id") Long postId
    ) {
        PostBookmarkResponse response = postService.deleteBookmark(
                userDetails.getUserId(),
                postId
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("북마크가 취소되었습니다.", response));
    }
}