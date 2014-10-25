package com.kozyrenko.danger.service;

/**
 * Created by dev on 10/25/14.
 */
public class SafeHouse {
    private double latitude;
    private double longitude;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SafeHouse{");
        sb.append("latitude=").append(latitude);
        sb.append(", longitude=").append(longitude);
        sb.append('}');
        return sb.toString();
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
