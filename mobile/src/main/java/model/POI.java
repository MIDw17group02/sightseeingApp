package model;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

/**
 * POI representation class.
 * This class wraps up content of a POI.
 * It implements the Comparable Interface and is compared based on the distance to the
 * current location.
 */
public class POI implements Comparable {

    private String name;
    private double longitude;
    private double latitude;
    private double rating; // Google rating from 0.0 to 5.0 stars.
    private String vicinity; // = Address of the poi
    private Bitmap photo; // Bitmap of a photo that is included in the google place.

    private String id;
    private String place_id;
    private String reference;

    // TODO update this usage by using the location!
    private double distance_to_start = -1.0; // Distance of the POI in kilo meters to the current position.

    // Indicate if the user wants to visit this POI.
    private boolean selected = false;

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof POI)) {
            return false;
        }

        POI poi2 = (POI) o;
        if (id != poi2.getId()) {
            return false;
        }
        if (place_id != poi2.getPlace_id()) {
            return false;
        }

        return true;
    }

    public double getDistanceToStart() {
        return distance_to_start;
    }

    public void setDistanceToStart(double distance_to_start) {
        this.distance_to_start = distance_to_start;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public int compareTo(@NonNull Object o) {

        POI other = (POI) o;
        if (getDistanceToStart() < other.getDistanceToStart()) {
            return -1;
        } else if(getDistanceToStart() > other.getDistanceToStart()) {
            return 1;
        }

        return 0;
    }
}
