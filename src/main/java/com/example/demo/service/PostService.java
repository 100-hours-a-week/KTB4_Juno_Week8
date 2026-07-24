package com.example.demo.service;

import com.example.demo.domain.Post;
import com.example.demo.domain.User;
import com.example.demo.domain.PostBookmark;
import com.example.demo.domain.PostBookmarkId;
import com.example.demo.domain.PostView;
import com.example.demo.domain.PostViewId;
import com.example.demo.dto.bookmark.PostBookmarkResponse;
import com.example.demo.dto.post.*;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.*;
import com.example.demo.domain.Comment;
import com.example.demo.dto.comment.UpdateCommentRequest;
import com.example.demo.dto.comment.UpdateCommentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.demo.dto.comment.CreateCommentRequest;
import com.example.demo.dto.comment.CreateCommentResponse;
import com.example.demo.dto.comment.DeleteCommentResponse;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookmarkRepository bookmarkRepository;
    private final PostViewRepository postViewRepository;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository,
            CommentRepository commentRepository,
            BookmarkRepository bookmarkRepository,
            PostViewRepository postViewRepository
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.postViewRepository = postViewRepository;
    }

    public CreatePostResponse createPost(Long userId, CreatePostRequest request) {
        User user = findLoginUser(userId);

        Post post = postRepository.save(
                new Post(
                        user,
                        request.getTitle(),
                        request.getContent(),
                        request.getImage()
                )
        );

        return new CreatePostResponse(post.getPostId());
    }

    @Transactional(readOnly = true)
    public PostListResponse getPostList() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<PostListItemResponse> posts = postRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(post -> {
                    User author = post.getAuthor();

                    return new PostListItemResponse(
                            post.getPostId(),
                            post.getTitle(),
                            post.getBookmarkCount(),
                            post.getCommentCount(),
                            post.getViewCount(),
                            post.getCreatedAt().format(formatter),
                            getDisplayNickname(author),
                            getDisplayProfileImage(author)
                    );
                })
                .toList();

        return new PostListResponse(posts);
    }

    public PostDetailResponse getPost(Long userId, Long postId) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "게시글을 찾을 수 없습니다."
                ));

        validateNotDeletedPost(post);

        increaseViewCountIfNeeded(userId, post);

        int viewCount = postRepository.findViewCountByPostId(postId);

        boolean bookmarked = isBookmarkedByUser(userId, postId);

        User author = post.getAuthor();

        List<PostDetailCommentResponse> comments =
                commentRepository.findAllByPostAndDeletedAtIsNull(post)
                        .stream()
                        .map(comment -> {
                            User commentAuthor = comment.getAuthor();

                            return new PostDetailCommentResponse(
                                    comment.getCommentId(),
                                    comment.getContent(),
                                    comment.getCreatedAt().format(formatter),
                                    getDisplayUserId(commentAuthor),
                                    getDisplayNickname(commentAuthor),
                                    getDisplayProfileImage(commentAuthor)
                            );
                        })
                        .toList();

        return new PostDetailResponse(
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getImage(),
                post.getBookmarkCount(),
                post.getCommentCount(),
                viewCount,
                bookmarked,
                post.getCreatedAt().format(formatter),
                author.getUserId(),
                getDisplayNickname(author),
                getDisplayProfileImage(author),
                comments
        );
    }

    public UpdatePostResponse updatePost(
            Long userId,
            Long postId,
            UpdatePostRequest request
    ) {
        findLoginUser(userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "게시글을 찾을 수 없습니다."
                ));

        validateNotDeletedPost(post);

        if (!post.getAuthor().getUserId().equals(userId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "게시글 수정 권한이 없습니다."
            );
        }

        post.update(
                request.getTitle(),
                request.getContent(),
                request.getImage()
        );

        return new UpdatePostResponse(post.getPostId());
    }

    public DeletePostResponse deletePost(Long userId, Long postId) {
        findLoginUser(userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "게시글을 찾을 수 없습니다."
                ));

        validateNotDeletedPost(post);

        if (!post.getAuthor().getUserId().equals(userId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "게시글 삭제 권한이 없습니다."
            );
        }

        List<Comment> comments =
                commentRepository.findAllByPostAndDeletedAtIsNull(post);

        for (Comment comment : comments) {
            comment.delete();
        }

        post.delete();

        return new DeletePostResponse(postId);
    }

    public CreateCommentResponse createComment(
            Long userId,
            Long postId,
            CreateCommentRequest request
    ) {
        User user = findLoginUser(userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "게시글을 찾을 수 없습니다."
                ));

        validateNotDeletedPost(post);

        Comment comment = commentRepository.save(
                new Comment(
                        post,
                        user,
                        request.getContent()
                )
        );

        postRepository.increaseCommentCount(postId);

        return new CreateCommentResponse(comment.getCommentId());
    }

    public UpdateCommentResponse updateComment(
            Long userId,
            Long postId,
            Long commentId,
            UpdateCommentRequest request
    ) {
        findLoginUser(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "댓글을 찾을 수 없습니다."
                ));

        validateNotDeletedComment(comment);
        validateNotDeletedPost(comment.getPost());

        if (!comment.getPost().getPostId().equals(postId)) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "댓글을 찾을 수 없습니다."
            );
        }

        if (!comment.getAuthor().getUserId().equals(userId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "댓글 수정 권한이 없습니다."
            );
        }

        comment.update(request.getContent());

        return new UpdateCommentResponse(comment.getCommentId());
    }

    public DeleteCommentResponse deleteComment(
            Long userId,
            Long postId,
            Long commentId
    ) {
        findLoginUser(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "댓글을 찾을 수 없습니다."
                ));

        validateNotDeletedComment(comment);
        validateNotDeletedPost(comment.getPost());

        if (!comment.getPost().getPostId().equals(postId)) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "댓글을 찾을 수 없습니다."
            );
        }

        if (!comment.getAuthor().getUserId().equals(userId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "댓글 삭제 권한이 없습니다."
            );
        }

        comment.delete();
        postRepository.decreaseCommentCount(postId);

        return new DeleteCommentResponse(commentId);
    }



    public PostBookmarkResponse createBookmark(
            Long userId,
            Long postId
    ) {
        User user = findLoginUser(userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "게시글을 찾을 수 없습니다."
                ));

        validateNotDeletedPost(post);

        PostBookmarkId bookmarkId =
                new PostBookmarkId(postId, userId);

        boolean alreadyBookmarked =
                bookmarkRepository.existsById(bookmarkId);

        if (!alreadyBookmarked) {
            bookmarkRepository.save(
                    new PostBookmark(post, user)
            );

            postRepository.increaseBookmarkCount(postId);
        }

        int bookmarkCount =
                postRepository.findBookmarkCountByPostId(postId);

        return new PostBookmarkResponse(
                post.getPostId(),
                bookmarkCount,
                true
        );
    }

    public PostBookmarkResponse deleteBookmark(
            Long userId,
            Long postId
    ) {
        findLoginUser(userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "게시글을 찾을 수 없습니다."
                ));

        validateNotDeletedPost(post);

        PostBookmarkId bookmarkId =
                new PostBookmarkId(postId, userId);

        boolean alreadyBookmarked =
                bookmarkRepository.existsById(bookmarkId);

        if (alreadyBookmarked) {
            bookmarkRepository.deleteById(bookmarkId);
            postRepository.decreaseBookmarkCount(postId);
        }

        int bookmarkCount =
                postRepository.findBookmarkCountByPostId(postId);

        return new PostBookmarkResponse(
                post.getPostId(),
                bookmarkCount,
                false
        );
    }
    private Long getDisplayUserId(User user) {
        if (user == null) {
            return null;
        }

        return user.getUserId();
    }

    private String getDisplayNickname(User user) {
        if (user == null) {
            return "탈퇴한 사용자";
        }

        return user.getNickname();
    }

    private String getDisplayProfileImage(User user) {
        if (user == null) {
            return null;
        }

        return user.getProfileImage();
    }


    private boolean isBookmarkedByUser(Long userId, Long postId) {
        return bookmarkRepository.existsById(
                new PostBookmarkId(postId, userId)
        );
    }

    private void increaseViewCountIfNeeded(Long userId, Post post) {
        User user = findLoginUser(userId);

        PostViewId postViewId =
                new PostViewId(post.getPostId(), user.getUserId());

        PostView postView = postViewRepository.findById(postViewId)
                .orElse(null);

        if (postView == null) {
            postViewRepository.save(new PostView(post, user));
            postRepository.increaseViewCount(post.getPostId());
            return;
        }

        LocalDateTime standardTime =
                LocalDateTime.now().minusHours(24);

        if (postView.canIncreaseViewCountAfter(standardTime)) {
            postRepository.increaseViewCount(post.getPostId());
            postView.updateLastViewedAt();
        }
    }

    private void validateNotDeletedPost(Post post) {
        if (post.getDeletedAt() != null) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "게시글을 찾을 수 없습니다."
            );
        }
    }

    private void validateNotDeletedComment(Comment comment) {
        if (comment.getDeletedAt() != null) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "댓글을 찾을 수 없습니다."
            );
        }
    }

    private User findLoginUser(Long userId) {
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "로그인이 필요합니다."
                ));
    }
}