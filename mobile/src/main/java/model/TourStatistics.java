package model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by de on 17.12.2017.
 */

public class TourStatistics {

    private double walkedDistance;
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
