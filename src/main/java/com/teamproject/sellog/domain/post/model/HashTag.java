package com.teamproject.sellog.domain.post.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "hash_tag")
public class HashTag {
    private UUID id;
    private UUID postId;
    private UUID productId;
    private String tagName;
}
