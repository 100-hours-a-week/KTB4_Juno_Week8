package com.example.demo.service;

import com.example.demo.domain.Post;
import com.example.demo.domain.User;
import com.example.demo.domain.PostBookmark;
import com.example.demo.domain.PostBookmarkId;
import com.example.demo.domain.PostView;
import com.example.demo.domain.PostViewId;
import com.example.demo.dto.bookmark.PostBookmarkResponse;
import com.example.demo.dto.bookmark.BookmarkListItemResponse;
import com.example.demo.dto.bookmark.BookmarkListResponse;
import com.example.demo.dto.post.*;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.*;
import com.example.demo.domain.Comment;
import com.example.demo.dto.comment.UpdateCommentRequest;
import com.example.demo.dto.comment.UpdateCommentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.comment.CreateCommentRequest;
import com.example.demo.dto.comment.CreateCommentResponse;
import com.example.demo.dto.comment.DeleteCommentResponse;
import com.example.demo.dto.category.CategoryItemResponse;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.demo.domain.Category;
import com.example.demo.domain.PostCategory;

import java.util.HashSet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookmarkRepository bookmarkRepository;
    private final PostViewRepository postViewRepository;
    private final CategoryRepository categoryRepository;
    private final PostCategoryRepository postCategoryRepository;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository,
            CommentRepository commentRepository,
            BookmarkRepository bookmarkRepository,
            PostViewRepository postViewRepository,
            CategoryRepository categoryRepository,
            PostCategoryRepository postCategoryRepository
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.postViewRepository = postViewRepository;
        this.categoryRepository = categoryRepository;
        this.postCategoryRepository = postCategoryRepository;
    }

    public CreatePostResponse createPost(Long userId, CreatePostRequest request) {
        User user = findLoginUser(userId);

        List<Long> categoryIds = request.getCategoryIds();

        List<Category> categories = List.of();

        if (categoryIds != null && !categoryIds.isEmpty()) {
            if (new HashSet<>(categoryIds).size() != categoryIds.size()) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "같은 카테고리를 중복해서 선택할 수 없습니다."
                );
            }

            categories = categoryRepository.findAllById(categoryIds);

            if (categories.size() != categoryIds.size()) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "존재하지 않는 카테고리가 포함되어 있습니다."
                );
            }
        }

        Post post = postRepository.save(
                new Post(
                        user,
                        request.getTitle(),
                        request.getContent(),
                        request.getImage()
                )
        );

        if (!categories.isEmpty()) {
            List<PostCategory> postCategories = categories.stream()
                    .map(category -> new PostCategory(post, category))
                    .toList();

            postCategoryRepository.saveAll(postCategories);
        }

        return new CreatePostResponse(post.getPostId());
    }

    @Transactional(readOnly = true)
    public PostListResponse getPostList() {
        return getPostList(null,null, "latest", 0, 10);
    }

    @Transactional(readOnly = true)
    public PostListResponse getPostList(String keyword) {
        return getPostList(keyword, null, "latest", 0, 10);
    }

    @Transactional(readOnly = true)
    public PostListResponse getPostList(
            String keyword,
            List<Long> categoryIds,
            String sort,
            int page,
            int size
    ) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String normalizedKeyword =
                keyword == null ? "" : keyword.trim();

        List<Long> normalizedCategoryIds =
                categoryIds == null ? List.of() : categoryIds;

        if (normalizedCategoryIds.size() > 3) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "카테고리는 최대 3개까지 선택할 수 있습니다."
            );
        }

        if (new HashSet<>(normalizedCategoryIds).size()
                != normalizedCategoryIds.size()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "같은 카테고리를 중복해서 선택할 수 없습니다."
            );
        }

        if (normalizedCategoryIds.stream()
                .anyMatch(categoryId ->
                        categoryId == null || categoryId <= 0)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "카테고리 ID는 양수여야 합니다."
            );
        }

        if (!normalizedCategoryIds.isEmpty()) {
            List<Category> categories =
                    categoryRepository.findAllById(normalizedCategoryIds);

            if (categories.size() != normalizedCategoryIds.size()) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "존재하지 않는 카테고리가 포함되어 있습니다."
                );
            }
        }

        if (page < 0) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "페이지 번호는 0 이상이어야 합니다."
            );
        }

        if (size < 1 || size > 100) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "페이지 크기는 1 이상 100 이하여야 합니다."
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                createSort(sort)
        );

        Page<Post> postPage;

        boolean hasKeyword = !normalizedKeyword.isBlank();
        boolean hasCategories = !normalizedCategoryIds.isEmpty();

        if (!hasKeyword && !hasCategories) {
            postPage =
                    postRepository.findAllByDeletedAtIsNull(pageable);

        } else if (hasKeyword && !hasCategories) {
            postPage =
                    postRepository.searchByKeyword(
                            normalizedKeyword,
                            pageable
                    );

        } else if (!hasKeyword) {
            postPage =
                    postRepository.findByCategoryIds(
                            normalizedCategoryIds,
                            normalizedCategoryIds.size(),
                            pageable
                    );

        } else {
            postPage =
                    postRepository.searchByKeywordAndCategoryIds(
                            normalizedKeyword,
                            normalizedCategoryIds,
                            normalizedCategoryIds.size(),
                            pageable
                    );
        }

        List<Post> pagePosts = postPage.getContent();

        List<Long> postIds = pagePosts.stream()
                .map(Post::getPostId)
                .toList();

        Map<Long, List<CategoryItemResponse>> categoriesByPostId;

        if (postIds.isEmpty()) {
            categoriesByPostId = Map.of();
        } else {
            categoriesByPostId =
                    postCategoryRepository
                            .findAllByPost_PostIdIn(postIds)
                            .stream()
                            .collect(Collectors.groupingBy(
                                    postCategory ->
                                            postCategory
                                                    .getPost()
                                                    .getPostId(),
                                    Collectors.mapping(
                                            postCategory ->
                                                    new CategoryItemResponse(
                                                            postCategory
                                                                    .getCategory()
                                                                    .getCategoryId(),
                                                            postCategory
                                                                    .getCategory()
                                                                    .getName()
                                                    ),
                                            Collectors.toList()
                                    )
                            ));
        }


        List<PostListItemResponse> posts = pagePosts
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
                            getDisplayProfileImage(author),
                            categoriesByPostId.getOrDefault(
                                    post.getPostId(),
                                    List.of()
                            )
                    );
                })
                .toList();

        return new PostListResponse(
                posts,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalPages(),
                postPage.getTotalElements(),
                postPage.isFirst(),
                postPage.isLast(),
                postPage.hasNext(),
                postPage.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    public BookmarkListResponse getBookmarkList(Long userId) {
        User user = findLoginUser(userId);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<PostBookmark> bookmarks =
                bookmarkRepository
                        .findAllByUserAndPost_DeletedAtIsNullOrderByCreatedAtDesc(user);

        List<Long> postIds = bookmarks.stream()
                .map(bookmark -> bookmark.getPost().getPostId())
                .toList();

        Map<Long, List<CategoryItemResponse>> categoriesByPostId;

        if (postIds.isEmpty()) {
            categoriesByPostId = Map.of();
        } else {
            categoriesByPostId =
                    postCategoryRepository
                            .findAllByPost_PostIdIn(postIds)
                            .stream()
                            .collect(Collectors.groupingBy(
                                    postCategory ->
                                            postCategory
                                                    .getPost()
                                                    .getPostId(),
                                    Collectors.mapping(
                                            postCategory ->
                                                    new CategoryItemResponse(
                                                            postCategory
                                                                    .getCategory()
                                                                    .getCategoryId(),
                                                            postCategory
                                                                    .getCategory()
                                                                    .getName()
                                                    ),
                                            Collectors.toList()
                                    )
                            ));
        }

        List<BookmarkListItemResponse> posts =
                bookmarks
                        .stream()
                        .map(bookmark -> {
                            Post post = bookmark.getPost();
                            User author = post.getAuthor();

                            return new BookmarkListItemResponse(
                                    post.getPostId(),
                                    post.getTitle(),
                                    post.getBookmarkCount(),
                                    post.getCommentCount(),
                                    post.getViewCount(),
                                    post.getCreatedAt().format(formatter),
                                    bookmark.getCreatedAt().format(formatter),
                                    getDisplayNickname(author),
                                    getDisplayProfileImage(author),
                                    categoriesByPostId.getOrDefault(
                                            post.getPostId(),
                                            List.of()
                                    )
                            );
                        })
                        .toList();

        return new BookmarkListResponse(posts);
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

        List<CategoryItemResponse> categories =
                postCategoryRepository
                        .findAllByPost(post)
                        .stream()
                        .map(postCategory -> new CategoryItemResponse(
                                postCategory.getCategory().getCategoryId(),
                                postCategory.getCategory().getName()
                        ))
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
                categories,
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

        List<Long> categoryIds = request.getCategoryIds();

        List<Category> categories = List.of();

        if (categoryIds != null) {
            if (new HashSet<>(categoryIds).size() != categoryIds.size()) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "같은 카테고리를 중복해서 선택할 수 없습니다."
                );
            }

            if (!categoryIds.isEmpty()) {
                categories = categoryRepository.findAllById(categoryIds);

                if (categories.size() != categoryIds.size()) {
                    throw new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "존재하지 않는 카테고리가 포함되어 있습니다."
                    );
                }
            }
        }

        post.update(
                request.getTitle(),
                request.getContent(),
                request.getImage()
        );

        if (categoryIds != null) {
            postCategoryRepository.deleteAllByPost(post);
            postCategoryRepository.flush();

            if (!categories.isEmpty()) {
                List<PostCategory> postCategories = categories.stream()
                        .map(category -> new PostCategory(post, category))
                        .toList();

                postCategoryRepository.saveAll(postCategories);
            }
        }

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

    private Sort createSort(String sort) {
        String normalizedSort =
                sort == null ? "latest" : sort.trim().toLowerCase();

        return switch (normalizedSort) {
            case "bookmarks" -> Sort.by(
                    Sort.Order.desc("bookmarkCount"),
                    Sort.Order.desc("createdAt")
            );
            case "views" -> Sort.by(
                    Sort.Order.desc("viewCount"),
                    Sort.Order.desc("createdAt")
            );
            case "comments" -> Sort.by(
                    Sort.Order.desc("commentCount"),
                    Sort.Order.desc("createdAt")
            );
            case "latest" -> Sort.by(
                    Sort.Order.desc("createdAt")
            );
            default -> throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "올바르지 않은 게시글 정렬 기준입니다."
            );
        };
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