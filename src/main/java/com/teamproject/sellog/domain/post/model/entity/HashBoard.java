package com.teamproject.sellog.domain.post.model.entity;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hash_tag_board")
public class HashBoard {

    @Id
    @Column(name = "post_id", nullable = false, insertable = false, updatable = false)
    private UUID postId;
    @Id
    @Column(name = "tag_id", nullable = false, insertable = false, updatable = false)
    private UUID tagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "id", nullable = false)
    @MapsId
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", referencedColumnName = "id", nullable = false)
    @MapsId
    private HashTag tag;
    // equals, hashcode

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof HashBoard)) {
            return false;
        }
        HashBoard hashBoard = (HashBoard) o;
        if ((this.postId != null && hashBoard.postId != null) && (this.tagId != null && hashBoard.tagId != null)) {
            return Objects.equals(this.postId, hashBoard.postId) && Objects.equals(this.tagId, hashBoard.tagId);
        }
        return Objects.equals(this.postId, hashBoard.postId) && Objects.equals(this.tagId, hashBoard.tagId);
    }

    @Override
    public int hashCode() {
        if (this.postId != null) {
            return Objects.hash(this.postId);
        }
        return Objects.hash(this.tagId);
    }
}