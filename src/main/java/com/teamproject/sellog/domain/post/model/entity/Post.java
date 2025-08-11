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

import com.teamproject.sellog.common.locationUtils.Location;
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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

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

    @Column(name = "price", nullable = false)
    private BigInteger price = BigInteger.ZERO;

    @Column(name = "place", nullable = false)
    private String place = " ";

    @Column(name = "location_latitude", nullable = true)
    private Double latitude;
    @Column(name = "location_longitude", nullable = true)
    private Double longitude;

    @Column(name = "location_point", nullable = true)
    private Point locationPoint;

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
        if (this.latitude != null && this.longitude != null) {
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            Coordinate coordinate = new Coordinate(this.longitude, this.latitude);
            this.locationPoint = geometryFactory.createPoint(coordinate);
        } else {
            this.locationPoint = null;
        }

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
        if (this.latitude != null && this.longitude != null) {
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            Coordinate coordinate = new Coordinate(this.longitude, this.latitude);
            this.locationPoint = geometryFactory.createPoint(coordinate);
        } else {
            this.locationPoint = null;
        }
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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PostFeedbackList> feedBacks = new HashSet<>();

    public boolean addFeedBack(PostFeedbackList feedBack, FeedBackType type) {
        for (PostFeedbackList feedbackList : this.feedBacks) {
            if (feedbackList.equals(feedBack)) {
                return false; // 피드백 종류 상관없이 존재하면 false
            }
        }
        this.feedBacks.add(feedBack);
        feedBack.setPost(this);
        feedBack.setType(type); // 받은 타입으로 추가
        return true;
    }

    public boolean removeFeedBack(PostFeedbackList feedBack, FeedBackType type) {
        PostFeedbackList postFeedbackList = null;
        for (PostFeedbackList feedbackList : this.feedBacks) {
            if (feedbackList.equals(feedBack)) {
                postFeedbackList = feedbackList; // 피드백 종류 상관없이 일단 탐색
                break;
            }
        }
        if (postFeedbackList != null && postFeedbackList.getType() == type) { // 종류까지 같으면 삭제
            this.feedBacks.remove(feedBack);
            return true;
        }
        return false;
    }

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