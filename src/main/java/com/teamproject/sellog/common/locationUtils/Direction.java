package com.teamproject.sellog.common.locationUtils;

public enum Direction {
    // 주요 8방향과 그에 해당하는 베어링 각도 (도 Degree 단위)
    NORTH(0.0), // 북쪽
    NORTHEAST(45.0), // 북동쪽
    EAST(90.0), // 동쪽
    SOUTHEAST(135.0), // 남동쪽
    SOUTH(180.0), // 남쪽
    SOUTHWEST(225.0), // 남서쪽
    WEST(270.0), // 서쪽
    NORTHWEST(315.0); // 북서쪽

    private final double bearing; // 해당 방향의 방위각 (0~360도 범위)

    Direction(double bearing) {
        this.bearing = bearing;
    }

    public double getBearing() {
        return bearing;
    }
}
