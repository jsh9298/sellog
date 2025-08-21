package com.teamproject.sellog.domain.auth.service.event;

import org.springframework.context.ApplicationEvent;

import com.teamproject.sellog.domain.user.model.entity.user.User;

public class UserDeletedEvent extends ApplicationEvent {
    private final User user;

    public UserDeletedEvent(Object source, User user) {
        super(source);
        this.user = user;

    }

    public User getUser() {
        return user;
    }
}