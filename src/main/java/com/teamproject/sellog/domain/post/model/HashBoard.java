package com.teamproject.sellog.domain.post.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "hash_tag_board")
public class HashBoard {
    private UUID id;
    private UUID postId;
    private UUID productId;
    private PostType type;
}
