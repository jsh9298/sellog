package com.teamproject.sellog.domain.post.model.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "hash_tag")
public class HashTag {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "tag", nullable = false, unique = true)
    private String tagName;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<HashBoard> hashBoard = new HashSet<>();

    public void addHash(HashBoard hashBoard) {
        this.hashBoard.add(hashBoard);
        if (hashBoard.getTag() != this) {
            hashBoard.setTag(this);
        }
    }

    public void removeHash(HashBoard hashBoard) {
        this.hashBoard.remove(hashBoard);
        if (hashBoard.getTag() == this) {
            hashBoard.setTag(null);
        }
    }

    // equals, hashcode

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof HashTag)) {
            return false;
        }
        HashTag hashTag = (HashTag) o;
        if (this.id != null && hashTag.id != null) {
            return Objects.equals(this.id, hashTag.id);
        }
        return Objects.equals(this.id, hashTag.id);
    }

    @Override
    public int hashCode() {
        if (this.id != null) {
            return Objects.hash(this.id);
        }
        return Objects.hash(this.tagName);
    }
}
