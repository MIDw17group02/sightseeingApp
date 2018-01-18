package model

import java.util.LinkedList

/**
 * This class wraps up the statistics for the current Tour.
 */
class TourStatistics {

    // Walked distance so far in meters.
    var walkedDistance: Double = 0.toDouble()
    // Tour duration til now in milliseconds since 1970.
    var walkedDuration: Long = 0
    private val seenPOIs: MutableList<POI>

    val visitedPOIs: Int
        get() = seenPOIs.size

    init {
        walkedDistance = 0.0
        walkedDuration = 0
        seenPOIs = LinkedList()
    }

    fun addVisitedPOI(poi: POI) {
        if (!seenPOIs.contains(poi)) {
            seenPOIs.add(poi)
        }
    }
}
