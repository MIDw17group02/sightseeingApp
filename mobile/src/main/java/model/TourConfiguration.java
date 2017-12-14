package model;

/**
 * This class wraps up all settings the user makes in the ConfigurationActivity.
 *
 * Created by dera on 14.12.17.
 */
public class TourConfiguration {

    // Indicate if the user wants to end the tour where he started it. Default: false
    private boolean roundTour;

    // Selected distance by the user [km]. Default: 2.0km
    private double distance;

    // Selected duration by the user [h]. Default: 1.0h
    private double duration;

    public TourConfiguration() {
        roundTour = false;
        distance = 2.0;
        duration = 1.0;
    }

    public boolean isRoundTour() {
        return roundTour;
    }

    public void setRoundTour(boolean roundTour) {
        this.roundTour = roundTour;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }
}
