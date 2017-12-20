package model

/**
 * This class wraps up all settings the user makes in the ConfigurationActivity.
 *
 * Created by dera on 14.12.17.
 */
class TourConfiguration {

    // Average walking speed in km/h
    var avgWalkSpeed: Double = 5.0

    // Indicate if the user wants to end the tour where he started it. Default: false
    var isRoundTour: Boolean = false

    // Selected Tempo Level
    // 0:slow, 1:normal, 2:fast
    var tempo: Int = 0.toInt()

    // Selected duration by the user [h]. Default: 1.0h
    var duration: Double = 0.toDouble()

    init {
        isRoundTour = false
        tempo = 1
        duration = 1.0
    }
}
