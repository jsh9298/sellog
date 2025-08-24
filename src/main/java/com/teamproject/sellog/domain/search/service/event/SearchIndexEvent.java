package com.teamproject.sellog.domain.search.service.event;

import com.teamproject.sellog.domain.search.model.entity.SearchIndex;
import org.springframework.context.ApplicationEvent;

public class SearchIndexEvent extends ApplicationEvent {
    private final SearchIndex searchIndex;

    public SearchIndexEvent(Object source, SearchIndex searchIndex) {
        super(source);
        this.searchIndex = searchIndex;
    }

    public SearchIndex getSearchIndex() {
        return searchIndex;
    }
}