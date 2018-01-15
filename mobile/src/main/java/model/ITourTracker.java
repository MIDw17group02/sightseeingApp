package model;

/**
 * Interface for tour observers.
 * Registered observers receive events, when POIs or tour end is reached.
 */
public interface ITourTracker {
    void OnTourEnd();
    void OnPOIReached(POI poi);
}
