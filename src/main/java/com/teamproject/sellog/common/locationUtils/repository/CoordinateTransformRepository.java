package com.teamproject.sellog.common.locationUtils.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

@Repository
public class CoordinateTransformRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public String transform(Point point, int targetSrid) {
        String sql = "SELECT ST_AsText(ST_Transform(ST_GeomFromText(:wkt, :sourceSrid), :targetSrid))";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("wkt", point.toText());
        query.setParameter("sourceSrid", point.getSRID());
        query.setParameter("targetSrid", targetSrid);
        return (String) query.getSingleResult();
    }
}
