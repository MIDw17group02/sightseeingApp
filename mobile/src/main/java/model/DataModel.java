package model;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 * Central data model class. It stores a sorted(!) ascending list of POIs in terms of their distance to the start coordinates and
 * also serves as a LocationListener. When the position changes the models position data gets updated.
 * Any ITourTrackers are notified, when an event occurs on a position change.
 * Implemented as Singleton Pattern, such that each Activity can access the data in its own lifecycle.
 */
public class DataModel implements LocationListener {

    private static DataModel dataModel; // Singleton instance

    private TourConfiguration tourConfiguration;
    private TourStatistics tourStatistics;

    private List<POI> nearbyPOIs;
    private Location lastLocation;

    private Location startLocation = null;
    private final double POI_NOTIFY_RANGE = 30.0;

    public List<ITourTracker> tourTrackers;

    private DataModel() {
        tourConfiguration = new TourConfiguration();
        tourStatistics = new TourStatistics();
        nearbyPOIs = new ArrayList<>();
        tourTrackers = new ArrayList<>();
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

    /*
     * Set the last location of the GPS Tracking.
     * Note: Only use this at the initializing in the beginning.
     */
    public void setLastLocation(Location lastLocation) {
        if (startLocation == null) {
            startLocation = lastLocation;
        }
        this.lastLocation = lastLocation;
    }

    /**
     * Return the last known location.
     *
     * @return Last known location.
     */
    public Location getLastLocation() {
        return lastLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(getClass().toString(), "Moved to " + location.toString());

        if (lastLocation != null) {

            tourStatistics.setWalkedDistance(tourStatistics.getWalkedDistance() + lastLocation.distanceTo(location));

            // Check if any unvisited POI was reached.
            for (POI poi : nearbyPOIs) {
                if (!poi.isVisited()) {
                    Location poiLocation = new Location(LocationManager.GPS_PROVIDER);
                    poiLocation.setLongitude(poi.getLongitude());
                    poiLocation.setLatitude(poi.getLatitude());
                    double delta = (double) location.distanceTo(poiLocation);
                    Log.d("GPS", String.valueOf(delta));
                    if (delta <= POI_NOTIFY_RANGE) {
                        Log.d("GPS", "POI " + poi.getName() + " visited.");
                        for (ITourTracker tourTracker : tourTrackers) {
                            tourTracker.OnPOIReached(poi);
                        }
                        poi.setVisited(true);
                        tourStatistics.addVisitedPOI(poi);
                    }
                }
            }

            // Check if tour end was reached.
            Log.d("DBG", "POIs " + String.valueOf(tourStatistics.getVisitedPOIs()) + " / " + getSelectedPOIs().size());
            if (tourStatistics.getVisitedPOIs() == getSelectedPOIs().size()) {
                if (!tourConfiguration.isRoundTour() || (tourConfiguration.isRoundTour() && location.distanceTo(startLocation) <= POI_NOTIFY_RANGE)) {
                    Log.d("GPS", "Tour end was reached.");
                    for (ITourTracker tourTracker : tourTrackers) {
                        tourTracker.OnTourEnd();
                    }
                }
            }

            // Update the last known location.
            lastLocation = location;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {}

    @Override
    public void onProviderDisabled(String s) {}

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
