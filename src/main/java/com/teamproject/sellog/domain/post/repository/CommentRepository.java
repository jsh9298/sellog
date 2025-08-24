package com.teamproject.sellog.domain.post.repository;

import com.teamproject.sellog.domain.post.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID>, JpaSpecificationExecutor<Comment> {
    // JpaSpecificationExecutor를 상속받아 Specification을 사용한 동적 쿼리 기능을 활성화합니다.
}