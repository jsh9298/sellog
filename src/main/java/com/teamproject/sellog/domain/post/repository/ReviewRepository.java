package com.teamproject.sellog.domain.post.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.post.model.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

}
