package com.teamproject.sellog.domain.post.model;

import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "review")
public class Review {
    private UUID reviewId;
    private UUID productId;
    private UUID userId;

    private Timestamp createAt;

    private Timestamp updateAt;
    private String content;

    private String score;
}
