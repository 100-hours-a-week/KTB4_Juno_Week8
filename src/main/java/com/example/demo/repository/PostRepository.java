package com.example.demo.repository;

import com.example.demo.domain.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "author")
    Page<Post> findAllByDeletedAtIsNull(Pageable pageable);

    @EntityGraph(attributePaths = "author")
    @Query("""
    select p
    from Post p
    where p.deletedAt is null
      and (
          lower(p.title) like lower(concat('%', :keyword, '%'))
          or lower(p.content) like lower(concat('%', :keyword, '%'))
      )
""")
    Page<Post> searchByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Modifying
    @Query("""
        update Post p
        set p.bookmarkCount = p.bookmarkCount + 1
        where p.postId = :postId
    """)
    int increaseBookmarkCount(@Param("postId") Long postId);

    @Modifying
    @Query("""
        update Post p
        set p.bookmarkCount = p.bookmarkCount - 1
        where p.postId = :postId
          and p.bookmarkCount > 0
    """)
    int decreaseBookmarkCount(@Param("postId") Long postId);

    @Modifying
    @Query("""
        update Post p
        set p.viewCount = p.viewCount + 1
        where p.postId = :postId
    """)
    int increaseViewCount(@Param("postId") Long postId);

    @Modifying
    @Query("""
        update Post p
        set p.commentCount = p.commentCount + 1
        where p.postId = :postId
    """)
    int increaseCommentCount(@Param("postId") Long postId);

    @Modifying
    @Query("""
        update Post p
        set p.commentCount = p.commentCount - 1
        where p.postId = :postId
          and p.commentCount > 0
    """)
    int decreaseCommentCount(@Param("postId") Long postId);

    @Query("""
        select p.bookmarkCount
        from Post p
        where p.postId = :postId
    """)
    int findBookmarkCountByPostId(@Param("postId") Long postId);

    @Query("""
        select p.viewCount
        from Post p
        where p.postId = :postId
    """)
    int findViewCountByPostId(@Param("postId") Long postId);
}