package com.teamproject.sellog.domain.post.model.entity;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "feedbacks")
public class PostFeedbackList {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "id", nullable = false)
    private Post post;

    private String userId;

    private FeedBackType type;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof PostFeedbackList)) {
            return false;
        }
        PostFeedbackList feedbackList = (PostFeedbackList) o;
        if (this.id != null && feedbackList.id != null) {
            return Objects.equals(this.id, feedbackList.id);
        }
        return Objects.equals(this.id, feedbackList.id);
    }

    @Override
    public int hashCode() {
        if (this.id != null) {
            return Objects.hash(this.id);
        }
        return Objects.hash(this.userId);
    }
}
