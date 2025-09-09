package com.teamproject.sellog.domain.search.service;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.domain.auth.service.event.UserCreatedEvent;
import com.teamproject.sellog.domain.auth.service.event.UserDeletedEvent;
import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.service.event.PostCreatedEvent;
import com.teamproject.sellog.domain.post.service.event.PostDeletedEvent;
import com.teamproject.sellog.domain.post.service.event.PostUpdatedEvent;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.user.service.event.UserUpdatedEvent;

@Service
public interface SearchIndexService {
    void handlePostCreated(PostCreatedEvent event);

    void handlePostUpdated(PostUpdatedEvent event);

    void handlePostDeleted(PostDeletedEvent event);

    void handleUserUpdated(UserUpdatedEvent event);

    void handleUserCreated(UserCreatedEvent event);

    void handleUserDeleted(UserDeletedEvent event);

    void updateSearchIndexForPost(Post post);

    void updateSearchIndexForUser(User user);
}
