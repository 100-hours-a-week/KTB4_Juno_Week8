package com.example.demo.repository;

import com.example.demo.domain.Post;
import com.example.demo.domain.PostBookmark;
import com.example.demo.domain.PostBookmarkId;
import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkRepository
        extends JpaRepository<PostBookmark, PostBookmarkId> {

    void deleteAllByPost(Post post);

    @EntityGraph(attributePaths = {"post", "post.author"})
    List<PostBookmark>
    findAllByUserAndPost_DeletedAtIsNullOrderByCreatedAtDesc(User user);
}