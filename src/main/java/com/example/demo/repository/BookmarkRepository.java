package com.example.demo.repository;

import com.example.demo.domain.Post;
import com.example.demo.domain.PostBookmark;
import com.example.demo.domain.PostBookmarkId;
import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<PostBookmark, PostBookmarkId> {

    void deleteAllByPost(Post bookmarkCount);

    boolean existsByPostAndUser(Post bookmarkCount, User user);
}