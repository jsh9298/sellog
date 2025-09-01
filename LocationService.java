package com.teamproject.sellog.domain.location.service;

import com.teamproject.sellog.common.locationUtils.Location;
import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.repository.PostSpatialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final PostSpatialRepository postSpatialRepository;

    public Page<Post> findPostsNearby(Location location, double distanceInMeters, Pageable pageable) {
        if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
            return Page.empty(pageable);
        }

        return postSpatialRepository.findPostsWithinDistance(
                location.getLongitude(),
                location.getLatitude(),
                distanceInMeters,
                pageable);
    }
}