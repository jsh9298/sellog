package com.teamproject.sellog.domain.recommend.service;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.service.event.PostCreatedEvent;
import com.teamproject.sellog.domain.post.service.event.PostDislikedEvent;
import com.teamproject.sellog.domain.post.service.event.PostLikedEvent;
import com.teamproject.sellog.domain.post.service.event.PostUpdatedEvent;
import com.teamproject.sellog.domain.post.service.event.ReviewCreatedEvent;
import com.teamproject.sellog.domain.post.service.event.ReviewDeletedEvent;
import com.teamproject.sellog.domain.search.model.entity.SearchIndex;
import com.teamproject.sellog.domain.search.service.event.SearchIndexEvent;

@Service
public interface RecommendTableService {
    void handlePostCreated(PostCreatedEvent event);

    void handlePostUpdated(PostUpdatedEvent event);

    void handleUpdateSearchIndex(SearchIndexEvent event);

    void updateItemForPost(Post post);

    void handlePostLiked(PostLikedEvent event);

    void handlePostDisliked(PostDislikedEvent event);

    void handleReviewCreated(ReviewCreatedEvent event);

    void handleReviewDeleted(ReviewDeletedEvent event);

    void updateItemForSearchIndex(SearchIndex searchIndex);

    void updateUserInteraction();
}
