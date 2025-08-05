package com.teamproject.sellog.domain.post.model.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;

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
    @EmbeddedId
    private HashBoardId id = new HashBoardId();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "id", nullable = false)
    @MapsId("postId")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", referencedColumnName = "id", nullable = false)
    @MapsId("tagId")
    private HashTag tag;

    public void setPost(Post post) {
        this.post = post;
        this.id.setPostId(post.getId());
    }

    public void setTag(HashTag tag) {
        this.tag = tag;
        this.id.setTagId(tag.getId());
    }
}