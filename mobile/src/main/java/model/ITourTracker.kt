package model

/**
 * Interface for tour observers.
 * Registered observers receive events, when POIs or tour end is reached.
 */
interface ITourTracker {
    fun OnTourEnd()
    fun OnPOIReached(poi: POI)
}
