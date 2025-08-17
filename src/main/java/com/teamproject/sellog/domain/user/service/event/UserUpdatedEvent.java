package com.teamproject.sellog.domain.user.service.event;

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