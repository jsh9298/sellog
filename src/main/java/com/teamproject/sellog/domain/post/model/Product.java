package com.teamproject.sellog.domain.post.model;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
public class Product {
    private UUID productId;
    private UUID userId;
    private String title;
    private BigInteger price;
    private String content;
    private String place;
    private Timestamp createAt;
    private Timestamp updateAt;
    private BigInteger likeCnt;
    private BigInteger readCnt;
    private String thumbnail;
}
