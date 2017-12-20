package model;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 * Central data model class. It stores a sorted(!) ascending list of POIs in terms of their distance to the start coordinates and
 * also serves as a LocationListener. When the position changes the models position data gets updated.
 * Implemented as Singleton Pattern, such that each Acitivty can access the data in its own lifecycle.
 */
public class DataModel implements LocationListener {

    private static DataModel dataModel; // Singleton instance
    private GoogleApiClient mGoogleApiClient;

    private TourConfiguration tourConfiguration;
    private TourStatistics tourStatistics;

    private List<POI> nearbyPOIs;
    private Location lastLocation;

    private DataModel() {
        tourConfiguration = new TourConfiguration();
        tourStatistics = new TourStatistics();
        nearbyPOIs = new ArrayList<>();
    }

    /*
     * Singleton method to get the instance.
     */
    public static DataModel getInstance() {
        if (dataModel == null) {
            dataModel = new DataModel();
        }

        return dataModel;
    }

    public boolean setLastKnownLocation(Location location) {
        if (location != null) {
            Log.e(getClass().getSimpleName(),location.toString());
            this.lastLocation = location;
            return true;
        }
        return false;
    }

    /**
     * Add a POI to the model.
     * This function performs a check if the poi is already included.
     *
     * @param poi
     */
    public void addPOI(POI poi) {
        if (poi != null && !nearbyPOIs.contains(poi)) {
            nearbyPOIs.add(poi);
            Collections.sort(nearbyPOIs);
        }
    }

    /**
     * Return the POI to a given index.
     *
     * @param index Position of the POI.
     * @return The corresponding POI.
     */
    public POI getPOI(int index) {
        if (index >= 0 && index < nearbyPOIs.size()) {
            return nearbyPOIs.get(index);
        }
        return null;
    }

    public void clearPOIs() {
        nearbyPOIs = new ArrayList<POI>();
    }

    /**
     * Return a list of all POIs that have the select flag set.
     *
     * @return A sub list of selected POIs.
     */
    public List<POI> getSelectedPOIs() {

        List<POI> selectedPOIs = new ArrayList<>();

        for (POI poi : nearbyPOIs) {
            if (poi.isSelected()) {
                selectedPOIs.add(poi);
            }
        }

        return selectedPOIs;
    }

    public List<POI> getNearbyPOIs() {
        return nearbyPOIs;
    }

    /**
     * Return the number of POIs stored in the model.
     *
     * @return #POIs
     */
    public int getPOIcount() {
        return nearbyPOIs.size();
    }

    /**
     * Return the last known location.
     *
     * @return Last known location.
     */
    public Location getLastLocation() {
        return lastLocation;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(getClass().toString(), "Got new location " + location.toString());
        lastLocation = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public TourConfiguration getTourConfiguration() {
        return tourConfiguration;
    }

    public void setTourConfiguration(TourConfiguration tourConfiguration) {
        this.tourConfiguration = tourConfiguration;
    }

    public TourStatistics getTourStatistics() {
        return tourStatistics;
    }

    public void setTourStatistics(TourStatistics tourStatistics) {
        this.tourStatistics = tourStatistics;
    }
}
