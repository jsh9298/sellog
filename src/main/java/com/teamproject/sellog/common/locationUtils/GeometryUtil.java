package com.teamproject.sellog.common.locationUtils;

public class GeometryUtil {
    private static final double EARTH_RADIUS = 6371.01;

    public static Location calculateByDirection(Double baseLatitude, Double baseLongitude, Double distance,
            Double bearing) {
        Double radianLatitude = toRadian(baseLatitude);
        Double radianLongitude = toRadian(baseLongitude);
        Double radianAngle = toRadian(bearing);
        Double distanceRadius = distance / EARTH_RADIUS;

        Double latitude = Math.asin(sin(radianLatitude) * cos(distanceRadius) +
                cos(radianLatitude) * sin(distanceRadius) * cos(radianAngle));
        Double longitude = radianLongitude + Math.atan2(sin(radianAngle) * sin(distanceRadius) *
                cos(radianLatitude), cos(distanceRadius) - sin(radianLatitude) * sin(latitude));

        longitude = normalizeLongitude(longitude);
        return new Location(toDegree(latitude), toDegree(longitude));
    }

    public static double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        double dLat = toRadian(lat2 - lat1);
        double dLon = toRadian(lon2 - lon1);

        double a = sin(dLat / 2) * sin(dLat / 2)
                + Math.cos(toRadian(lat1)) * cos(toRadian(lat2)) * sin(dLon / 2) * sin(dLon / 2);
        double b = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * b * 1000; // m
    }

    public static Double toRadian(Double coordinate) { // 테스트 후 private으로
        return coordinate * Math.PI / 180.0;
    }

    public static Double toDegree(Double coordinate) {// 테스트 후 private으로
        return coordinate * 180.0 / Math.PI;
    }

    public static Double sin(Double coordinate) {// 테스트 후 private으로
        return Math.sin(coordinate);
    }

    public static Double cos(Double coordinate) {// 테스트 후 private으로
        return Math.cos(coordinate);
    }

    public static Double normalizeLongitude(Double longitude) {// 테스트 후 private으로
        return (longitude + 540) % 360 - 180;
    }
}
