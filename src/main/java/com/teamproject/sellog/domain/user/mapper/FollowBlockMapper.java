package com.teamproject.sellog.domain.user.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.teamproject.sellog.domain.user.model.dto.response.BlockResponse;
import com.teamproject.sellog.domain.user.model.dto.response.FollowerResponse;
import com.teamproject.sellog.domain.user.model.entity.friend.Block;
import com.teamproject.sellog.domain.user.model.entity.friend.Follow;

@Mapper(componentModel = "spring")
public interface FollowBlockMapper {

    @Mapping(source = "followed.userId", target = "userId")
    @Mapping(source = "followed.userProfile.nickname", target = "nickname")
    @Mapping(source = "followed.userProfile.profileThumbURL", target = "profileThumbURL")
    @Mapping(source = "followed.userProfile.profileMessage", target = "profileMessage")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "createAt", target = "createAt")
    FollowerResponse toFollowerResponse(Follow follow);

    List<FollowerResponse> toFollowerResponseList(Set<Follow> following);

    @Mapping(source = "blocked.userId", target = "userId")
    @Mapping(source = "blocked.userProfile.nickname", target = "nickname")
    @Mapping(source = "blocked.userProfile.profileThumbURL", target = "profileThumbURL")
    @Mapping(source = "blocked.userProfile.profileMessage", target = "profileMessage")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "createAt", target = "createAt")
    BlockResponse toBlockResponse(Block block);

    List<BlockResponse> toBlockResponseList(Set<Block> blocking);
}