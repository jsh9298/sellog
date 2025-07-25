package com.teamproject.sellog.domain.post.model.entity;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.teamproject.sellog.domain.user.model.entity.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "feed")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "type", nullable = false)
    private PostType postType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "price", nullable = false) // null처리 어카노..
    private BigInteger price = BigInteger.ZERO;

    @Column(name = "place", nullable = false) // null처리 어카노..
    private String place = " ";

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "create_at", nullable = false)
    private Timestamp createAt;

    @Column(name = "update_at")
    private Timestamp updateAt;

    @Column(name = "like_cnt", nullable = false)
    private BigInteger likeCnt = BigInteger.ZERO;

    @Column(name = "read_cnt", nullable = false)
    private BigInteger readCnt = BigInteger.ZERO;

    @Column(name = "thumbnail")
    private String thumbnail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = true)
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new LinkedList<>();

    public void addReview(Review review) {
        this.reviews.add(review);
        if (review.getPost() != this) {
            review.setPost(this);
        }
    }

    public void removeReview(Review review) {
        this.reviews.remove(review);
        if (review.getPost() == this) {
            review.setPost(null);
        }
    }

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new LinkedList<>(); // 게시글에 달린 댓글들

    public void addComment(Comment comment) {
        this.comments.add(comment);
        if (comment.getPost() != this) {
            comment.setPost(this);
        }
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        if (comment.getPost() == this) {
            comment.setPost(null);
        }
    }

    @PrePersist
    public void onCreate() {
        if (this.createAt == null) {
            this.createAt = Timestamp.valueOf(LocalDateTime.now());
        }
        if (this.likeCnt == null)
            this.likeCnt = BigInteger.ZERO;
        if (this.readCnt == null)
            this.readCnt = BigInteger.ZERO;
    }

    @PreUpdate
    public void onUpdate() {
        this.updateAt = Timestamp.valueOf(LocalDateTime.now());
    }

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<HashBoard> hashBoard = new HashSet<>();

    public void addHash(HashBoard hashBoard) {
        this.hashBoard.add(hashBoard);
        hashBoard.setPost(this);
    }

    public void removeHash(HashBoard hashBoard) {
        this.hashBoard.remove(hashBoard);
        hashBoard.setPost(null);
    }

    // equals, hashcode

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Post)) {
            return false;
        }
        Post post = (Post) o;
        if (this.id != null && post.id != null) {
            return Objects.equals(this.id, post.id);
        }
        return Objects.equals(this.id, post.id);
    }

    @Override
    public int hashCode() {
        if (this.id != null) {
            return Objects.hash(this.id);
        }
        return Objects.hash(this.author);
    }
}