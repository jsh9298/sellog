package com.teamproject.sellog.domain.user.model.entity.user;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.teamproject.sellog.domain.post.model.entity.Comment;
import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.model.entity.Review;
import com.teamproject.sellog.domain.user.model.entity.friend.Block;
import com.teamproject.sellog.domain.user.model.entity.friend.Follow;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    private String userId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "password_salt", nullable = false)
    private String passwordSalt;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "create_at", nullable = false, updatable = false)
    private Timestamp createAt;

    @Column(name = "last_login", nullable = false)
    private Timestamp lastLogin;

    @Column(name = "account_visibility", nullable = false)
    private AccountVisibility accountVisibility;

    @PrePersist
    public void onCreate() {
        if (this.createAt == null) {
            this.createAt = Timestamp.valueOf(LocalDateTime.now());
        }
    }

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserPrivate userPrivate;

    public void setUserPrivate(UserPrivate userPrivate) {
        if (userPrivate == null) {
            if (this.userPrivate != null) {
                this.userPrivate.setUser(null);
            }
        } else {
            userPrivate.setUser(this);
        }
        this.userPrivate = userPrivate;
    }

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    public void setUserProfile(UserProfile userProfile) {
        if (userProfile == null) {
            if (this.userProfile != null) {
                this.userProfile.setUser(null);
            }
        } else {
            userProfile.setUser(this);
        }
        this.userProfile = userProfile;
    }

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follow> following = new HashSet<>();

    public boolean addFollowing(User followedUser) {
        boolean isBlocked = false;
        for (Block block : this.blocking) {
            if (block.getBlocked().equals(followedUser)) {
                isBlocked = true;
                break;
            }
        }
        if (isBlocked) {
            return false;
        }

        boolean alreadyFollowing = false;
        for (Follow follow : this.following) {
            if (follow.getFollowed().equals(followedUser)) {
                alreadyFollowing = true;
                break;
            }
        }
        if (alreadyFollowing) {
            return false;
        }
        Follow followToAdd = new Follow();
        followToAdd.setFollower(this);
        followToAdd.setFollowed(followedUser);
        this.following.add(followToAdd);
        return true;
    }

    public boolean removeFollowing(User followedUser) {
        Follow followToRemove = null;
        for (Follow follow : this.following) {
            if (follow.getFollowed().equals(followedUser)) {
                followToRemove = follow;
                break;
            }
        }
        if (followToRemove != null) {
            this.following.remove(followToRemove);
            return true;
        }
        return false;
    }

    @OneToMany(mappedBy = "blocking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Block> blocking = new HashSet<>();

    public boolean addBlocking(User blockedUser) {
        boolean alreadyBlocking = false;
        for (Block block : this.blocking) {
            if (block.getBlocked().equals(blockedUser)) {
                alreadyBlocking = true;
                break;
            }
        }
        if (alreadyBlocking) {
            return false;
        }
        boolean isFollowing = false;
        for (Follow follow : this.following) {
            if (follow.getFollowed().equals(blockedUser)) {
                isFollowing = true;
                break;
            }
        }
        Block newBlock = new Block();
        newBlock.setBlocking(this);
        newBlock.setBlocked(blockedUser);
        this.blocking.add(newBlock);
        if (isFollowing) {
            this.removeFollowing(blockedUser);
        }
        return true;
    }

    public boolean removeBlocking(User blockedUser) {
        Block blockToRemove = null;
        for (Block block : this.blocking) {
            if (block.getBlocked().equals(blockedUser)) {
                blockToRemove = block;
                break;
            }
        }
        if (blockToRemove != null) {
            this.blocking.remove(blockToRemove);
            return true;
        }
        return false;
    }

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private Set<Post> posts = new HashSet<>();

    public void addPost(Post post) {
        this.posts.add(post);
        if (post.getAuthor() != this) {
            post.setAuthor(this);
        }
    }

    public void removePost(Post post) {
        this.posts.remove(post);
        if (post.getAuthor() == this) {
            post.setAuthor(null);
        }
    }

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private Set<Comment> comments = new HashSet<>();

    public void addComment(Comment comment) {
        this.comments.add(comment);
        if (comment.getAuthor() != this) {
            comment.setAuthor(this);
        }
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        if (comment.getAuthor() == this) {
            comment.setAuthor(null);
        }
    }

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private Set<Review> reviews = new HashSet<>();

    public void addReview(Review review) {
        this.reviews.add(review);
        if (review.getAuthor() != this) {
            review.setAuthor(this);
        }
    }

    public void removeReview(Review review) {
        this.reviews.remove(review);
        if (review.getAuthor() == this) {
            review.setAuthor(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof User)) {
            return false;
        }
        User user = (User) o;
        if (this.id != null && user.id != null) {
            return Objects.equals(this.id, user.id);
        }
        return Objects.equals(this.userId, user.userId);
    }

    @Override
    public int hashCode() {
        if (this.id != null) {
            return Objects.hash(this.id);
        }
        return Objects.hash(this.userId);
    }
}
