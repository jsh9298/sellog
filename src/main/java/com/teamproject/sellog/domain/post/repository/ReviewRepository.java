package com.teamproject.sellog.domain.post.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.model.entity.Review;
import com.teamproject.sellog.domain.user.model.entity.user.User;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByPostAndAuthor(Post post, User Author);

    boolean existsByAuthor(User Author);
}