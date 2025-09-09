package com.teamproject.sellog.domain.recommend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.domain.recommend.model.Item;

@Service
public interface RecommendationService {
    List<Item> getRecommendationsFromPython(UUID userId);
}
