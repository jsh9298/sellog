package com.teamproject.sellog.domain.post.model;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "feed")
public class post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id")
    private UUID feedId;

    private String title;

    private String content;

    private Timestamp create_time;

    private Timestamp update_time;

    private BigInteger heart_cnt;

    private BigInteger view_cnt;
}
