package com.teamproject.sellog.domain.post.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.post.model.entity.HashTag;

@Repository
public interface TagRepository extends JpaRepository<HashTag, UUID> {
    Optional<HashTag> findByTagName(String tagname);
}
