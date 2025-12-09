package com.teadelivery.ordercatalog.common.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.math.BigDecimal;

/**
 * Utility class for PostGIS geometry operations
 * SRID 4326 = WGS84 (standard GPS coordinate system)
 */
public final class GeometryUtils {

    private static final int SRID_WGS84 = 4326;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), SRID_WGS84);

    private GeometryUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a PostGIS Point from latitude and longitude
     * 
     * @param latitude  Latitude (Y coordinate)
     * @param longitude Longitude (X coordinate)
     * @return Point geometry with SRID 4326
     */
    public static Point createPoint(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return createPoint(latitude.doubleValue(), longitude.doubleValue());
    }

    /**
     * Create a PostGIS Point from latitude and longitude
     * 
     * @param latitude  Latitude (Y coordinate)
     * @param longitude Longitude (X coordinate)
     * @return Point geometry with SRID 4326
     */
    public static Point createPoint(double latitude, double longitude) {
        // Note: In PostGIS, coordinates are (X, Y) which is (longitude, latitude)
        Coordinate coordinate = new Coordinate(longitude, latitude);
        Point point = GEOMETRY_FACTORY.createPoint(coordinate);
        point.setSRID(SRID_WGS84);
        return point;
    }

    /**
     * Get latitude from a Point
     * 
     * @param point PostGIS Point
     * @return Latitude (Y coordinate) or null if point is null
     */
    public static BigDecimal getLatitude(Point point) {
        if (point == null) {
            return null;
        }
        return BigDecimal.valueOf(point.getY());
    }

    /**
     * Get longitude from a Point
     * 
     * @param point PostGIS Point
     * @return Longitude (X coordinate) or null if point is null
     */
    public static BigDecimal getLongitude(Point point) {
        if (point == null) {
            return null;
        }
        return BigDecimal.valueOf(point.getX());
    }

    /**
     * Format Point as JSON string for API responses
     * 
     * @param point PostGIS Point
     * @return JSON string like {"lat":12.34,"lng":56.78} or null
     */
    public static String toJson(Point point) {
        if (point == null) {
            return null;
        }
        return String.format("{\"lat\":%s,\"lng\":%s}", point.getY(), point.getX());
    }
}
