package com.teamproject.sellog.common.locationUtils;

import com.teamproject.sellog.domain.post.model.entity.Post;

public class UserMapDistance {
    // 탐색 거리 (km 단위)
    /**
     * 기준 지점에서 최대 반경이 되는 북동쪽 좌표를 반환합니다.
     */
    public static Location aroundCustomerNortheastDot(Location customerLocation, double searchDistance) {

        double nowLatitude = customerLocation.getLatitude(); // 현재 위도 = y 좌표
        double nowLongitude = customerLocation.getLongitude(); // 현재 경도 = x 좌표

        return GeometryUtil.calculateByDirection(nowLatitude, nowLongitude, searchDistance,
                Direction.NORTHEAST.getBearing());
    }

    /**
     * 기준 지점에서 최대 거리가 되는 남서쪽 좌표를 반환합니다.
     */
    public static Location aroundCustomerSouthwestDot(Location customerLocation, double searchDistance) {

        double nowLatitude = customerLocation.getLatitude(); // 현재 위도 = y 좌표
        double nowLongitude = customerLocation.getLongitude(); // 현재 경도 = x 좌표

        return GeometryUtil.calculateByDirection(nowLatitude, nowLongitude, searchDistance,
                Direction.SOUTHWEST.getBearing());
    }

    public static double calculateDistance(Location customerLocation, Post post) {

        double nowLatitude = customerLocation.getLatitude(); // 현재 위도 = y 좌표
        double nowLongitude = customerLocation.getLongitude(); // 현재 경도 = x 좌표

        double storeLatitude = post.getLatitude(); // 현재 위도 = y 좌표
        double storeLongitude = post.getLongitude(); // 현재 경도 = x 좌표

        return GeometryUtil.calculateDistance(nowLatitude, nowLongitude, storeLatitude, storeLongitude);
    }
}
