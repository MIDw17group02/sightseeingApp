package model;

import java.util.LinkedList;
import java.util.List;

/**
 * This class wraps up the statistics for the current Tour.
 */
public class TourStatistics {

    // Walked distance so far in meters.
    private double walkedDistance;
    // Tour duration til now in milliseconds since 1970.
    private long walkedDuration;
    private List<POI> seenPOIs;

    public TourStatistics() {
        walkedDistance = 0.0;
        walkedDuration = 0;
        seenPOIs = new LinkedList<>();
    }

    public double getWalkedDistance() {
        return walkedDistance;
    }

    public void setWalkedDistance(double walkedDistance) {
        this.walkedDistance = walkedDistance;
    }

    public long getWalkedDuration() {
        return walkedDuration;
    }

    public void setWalkedDuration(long walkedDuration) {
        this.walkedDuration = walkedDuration;
    }

    public void addVisitedPOI(POI poi) {
        if (!seenPOIs.contains(poi)) {
            seenPOIs.add(poi);
        }
    }

    public int getVisitedPOIs() {
        return seenPOIs.size();
    }
}
