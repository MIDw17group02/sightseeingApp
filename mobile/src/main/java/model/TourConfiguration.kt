package model

/**
 * This class wraps up all settings the user makes in the ConfigurationActivity.
 *
 * Created by dera on 14.12.17.
 */
class TourConfiguration {

    // Indicate if the user wants to end the tour where he started it. Default: false
    var isRoundTour: Boolean = false

    // Selected distance by the user [km]. Default: 2.0km
    var distance: Double = 0.toDouble()

    // Selected duration by the user [h]. Default: 1.0h
    var duration: Double = 0.toDouble()

    init {
        isRoundTour = false
        distance = 2.0
        duration = 1.0
    }
}
