package com.teamproject.sellog.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.teamproject.sellog.domain.post.model.dto.request.PostRequestDto;
import com.teamproject.sellog.domain.post.model.dto.response.PostListResponseDto;
import com.teamproject.sellog.domain.post.model.dto.response.PostResponseDto;
import com.teamproject.sellog.domain.post.model.entity.Post;

@Mapper(componentModel = "spring")
public interface PostMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePostFromRequest(@MappingTarget Post post, PostRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PostResponseDto EntityToResponse(Post post, List<String> tagNames);

    PostListResponseDto toPostResponse(Post post);
}

/*
 * 
 * @Mapping(source = "followed.userId", target = "userId")
 * 
 * @Mapping(source = "followed.userProfile.nickname", target = "nickname")
 * 
 * @Mapping(source = "followed.userProfile.profileThumbURL", target =
 * "profileThumbURL")
 * 
 * @Mapping(source = "followed.userProfile.profileMessage", target =
 * "profileMessage")
 * 
 * @Mapping(source = "id", target = "id")
 * 
 * @Mapping(source = "createAt", target = "createAt")
 * FollowerResponse toFollowerResponse(Follow follow);
 * 
 * List<FollowerResponse> toFollowerResponseList(Set<Follow> following);
 * 
 */