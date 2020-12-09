package com.alexlim.touristspotfinder.model;

public class LocationItem {

    private String mLocationId;
    private String mLocationName;
    private String mLocationAddress;
    private String mLocationDesc;
    private String mLocationCategory;
    private double mLocationLatitude;
    private double mLocationLongitude;
    private boolean mLocationExist;
    private float mDistance;

    public LocationItem(String locationId, String locationName, String locationAddress,
                        String locationDesc, String locationCategory, double locationLatitude,
                        double locationLongitude, boolean locationExist, float distance) {
        mLocationId = locationId;
        mLocationName = locationName;
        mLocationAddress = locationAddress;
        mLocationDesc = locationDesc;
        mLocationCategory = locationCategory;
        mLocationLatitude = locationLatitude;
        mLocationLongitude = locationLongitude;
        mLocationExist = locationExist;
        mDistance = distance;
    }

    public String getLocationId() {
        return mLocationId;
    }

    public String getLocationName() {
        return mLocationName;
    }

    public String getLocationAddress() {
        return mLocationAddress;
    }

    public String getLocationDesc() {
        return mLocationDesc;
    }

    public String getCategory() {
        return mLocationCategory;
    }

    public double getLatitude() {
        return mLocationLatitude;
    }

    public double getLongitude() {
        return mLocationLongitude;
    }

    public boolean getLocationExist() {
        return mLocationExist;
    }

    public float getDistance() {
        return mDistance;
    }
}
