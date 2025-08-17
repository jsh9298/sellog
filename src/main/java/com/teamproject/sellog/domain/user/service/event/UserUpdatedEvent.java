package com.teamproject.sellog.domain.user.service.event;

import org.springframework.context.ApplicationEvent;

import com.teamproject.sellog.domain.user.model.entity.user.User;

public class UserUpdatedEvent extends ApplicationEvent {
    private final User user;

    public UserUpdatedEvent(Object source, User user) {
        super(source);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}