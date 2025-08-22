package com.teamproject.sellog.domain.recommend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.teamproject.sellog.domain.recommend.model.Item;
import com.teamproject.sellog.domain.recommend.repository.ItemRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final WebClient webClient;
    private final ItemRepository itemRepository;

    public RecommendationService(WebClient.Builder webClientBuilder,
            ItemRepository itemRepository,
            @Value("${ml.service.url}") String mlServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(mlServiceUrl).build();
        this.itemRepository = itemRepository;
    }

    public List<Item> getRecommendationsFromPython(UUID userId) {

        List<UUID> recommendedIds = webClient.post()
                .uri("/")
                .bodyValue(userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UUID>>() {
                })
                .doOnError(e -> System.err.println("[Spring Boot] Error calling ML Service: " + e.getMessage()))
                .onErrorReturn(Collections.emptyList())
                .block(); // MVC 기반이므로 block() 사용 가능

        if (recommendedIds == null || recommendedIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Item> items = itemRepository.findAllById(recommendedIds);

        // 순서 보장 (DB에서 나온 순서는 랜덤일 수 있음)
        Map<UUID, Item> itemMap = items.stream()
                .collect(Collectors.toMap(Item::getId, i -> i));

        return recommendedIds.stream()
                .map(itemMap::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
