package model;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by de on 01.12.2017.
 */
public class DataModel implements LocationListener {

    private static DataModel dataModel;

    private List<POI> nearbyPOIs;
    private Location lastLocation;

    private DataModel() {
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

    public void addPOI(POI poi) {
        if (poi != null && !nearbyPOIs.contains(poi)) {
            nearbyPOIs.add(poi);
            Collections.sort(nearbyPOIs);
        }
    }

    public POI getPOI(int index) {
        if (index >= 0 && index < nearbyPOIs.size()) {
            return nearbyPOIs.get(index);
        }
        return null;
    }

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

    public int getPOIcount() {
        return nearbyPOIs.size();
    }

    public Location getLastLocation() {
        return lastLocation;
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
}
