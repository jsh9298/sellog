package com.teamproject.sellog.domain.post.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.teamproject.sellog.domain.post.model.dto.request.ReviewRequest;
import com.teamproject.sellog.domain.post.model.dto.response.ReviewResponseDto;
import com.teamproject.sellog.domain.post.model.entity.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateReviewFromRequest(@MappingTarget Review review, ReviewRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ReviewResponseDto EntityToResponse(Review review);
}
