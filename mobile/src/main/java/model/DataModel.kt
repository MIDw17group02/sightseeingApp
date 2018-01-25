package model

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log

import java.util.ArrayList
import java.util.Collections


/**
 * Central data model class. It stores a sorted(!) ascending list of POIs in terms of their distance to the start coordinates and
 * also serves as a LocationListener. When the position changes the models position data gets updated.
 * Any ITourTrackers are notified, when an event occurs on a position change.
 * Implemented as Singleton Pattern, such that each Activity can access the data in its own lifecycle.
 */
class DataModel private constructor() : LocationListener {

    var tourConfiguration: TourConfiguration? = null
    var tourStatistics: TourStatistics? = null

    private var nearbyPOIs: MutableList<POI>? = null
    var visitedPOIs: MutableList<POI>? = null
    private var lastLocation: Location? = null

    private var startLocation: Location? = null
    private val POI_NOTIFY_RANGE = 30.0

    var tourTrackers: MutableList<ITourTracker>

    /**
     * Return a list of all POIs that have the select flag set.
     *
     * @return A sub list of selected POIs.
     */
    val selectedPOIs: List<POI>
        get() {

            val selectedPOIs = ArrayList<POI>()

            for (poi in nearbyPOIs!!) {
                if (poi.isSelected) {
                    selectedPOIs.add(poi)
                }
            }

            return selectedPOIs
        }

    /**
     * Return the number of POIs stored in the model.
     *
     * @return #POIs
     */
    val poIcount: Int
        get() = nearbyPOIs!!.size

    init {
        tourConfiguration = TourConfiguration()
        tourStatistics = TourStatistics()
        nearbyPOIs = ArrayList()
        tourTrackers = ArrayList()
        visitedPOIs = ArrayList()
    }

    /**
     * Add a POI to the model.
     * This function performs a check if the poi is already included.
     *
     * @param poi
     */
    fun addPOI(poi: POI?) {
        if (poi != null && !nearbyPOIs!!.contains(poi)) {
            nearbyPOIs!!.add(poi)
            Collections.sort(nearbyPOIs!!)
        }
    }

    /**
     * Return the POI to a given index.
     *
     * @param index Position of the POI.
     * @return The corresponding POI.
     */
    fun getPOI(index: Int): POI? {
        return if (index >= 0 && index < nearbyPOIs!!.size) {
            nearbyPOIs!![index]
        } else null
    }

    fun clearPOIs() {
        nearbyPOIs = ArrayList()
    }

    fun getNearbyPOIs(): List<POI>? {
        return nearbyPOIs
    }

    /*
     * Set the last location of the GPS Tracking.
     * Note: Only use this at the initializing in the beginning.
     */
    fun setLastLocation(lastLocation: Location) {
        if (startLocation == null) {
            startLocation = lastLocation
        }
        this.lastLocation = lastLocation
    }

    /**
     * Return the last known location.
     *
     * @return Last known location.
     */
    fun getLastLocation(): Location? {
        return lastLocation
    }

    override fun onLocationChanged(location: Location) {
        Log.d(javaClass.toString(), "Moved to " + location.toString())

        if (lastLocation != null) {

            tourStatistics!!.walkedDistance = tourStatistics!!.walkedDistance + lastLocation!!.distanceTo(location)

            // Check if any unvisited POI was reached.
            for (poi in nearbyPOIs!!) {
                if (!poi.isVisited) {
                    val poiLocation = Location(LocationManager.GPS_PROVIDER)
                    poiLocation.longitude = poi.longitude
                    poiLocation.latitude = poi.latitude
                    val delta = location.distanceTo(poiLocation).toDouble()
                    Log.d("GPS", delta.toString())
                    if (delta <= POI_NOTIFY_RANGE) {
                        Log.d("GPS", "POI " + poi.name + " visited.")
                        for (tourTracker in tourTrackers) {
                            tourTracker.OnPOIReached(poi)
                        }
                        poi.isVisited = true
                        tourStatistics!!.addVisitedPOI(poi)
                    }
                }
            }

            // Check if tour end was reached.
            Log.d("DBG", "POIs " + tourStatistics!!.visitedPOIs.toString() + " / " + selectedPOIs.size)
            if (tourStatistics!!.visitedPOIs == selectedPOIs.size) {
                if (!tourConfiguration!!.isRoundTour || tourConfiguration!!.isRoundTour && location.distanceTo(startLocation) <= POI_NOTIFY_RANGE) {
                    Log.d("GPS", "Tour end was reached.")
                    for (tourTracker in tourTrackers) {
                        tourTracker.OnTourEnd()
                    }
                }
            }

            // Update the last known location.
            lastLocation = location
        }
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}

    override fun onProviderEnabled(s: String) {}

    override fun onProviderDisabled(s: String) {}

    companion object {

        private var dataModel: DataModel? = null // Singleton instance

        /*
     * Singleton method to get the instance.
     */
        val instance: DataModel
            get() {
                if (dataModel == null) {
                    dataModel = DataModel()
                }

                return dataModel!!
            }
    }
}
