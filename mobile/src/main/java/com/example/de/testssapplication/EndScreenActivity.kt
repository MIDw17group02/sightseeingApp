package com.example.de.testssapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import java.util.concurrent.TimeUnit

import model.DataModel
import model.TourStatistics

class EndScreenActivity : AppCompatActivity() {

    private var distanceText: TextView? = null
    private var durationText: TextView? = null
    private var visitedPOIsText: TextView? = null

    private var endTourButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_screen2)

        title = getString(R.string.title_end_screen)

        distanceText = findViewById<View>(R.id.label_statistic_distance) as TextView
        durationText = findViewById<View>(R.id.label_statistic_duration) as TextView
        visitedPOIsText = findViewById<View>(R.id.label_poiCount) as TextView

        endTourButton = findViewById<View>(R.id.endTourButton) as Button
        endTourButton!!.setOnClickListener {
            // Finish the App
            finishAndRemoveTask()
            finishAffinity()
        }

        val tourStatistics = DataModel.instance.tourStatistics
        tourStatistics!!.walkedDuration = System.currentTimeMillis() - tourStatistics.walkedDuration
        val time = tourStatistics.walkedDuration
        var hours = ""
        val hourValue = TimeUnit.MILLISECONDS.toHours(time)
        if (hourValue != 0L) {
            hours += hourValue.toString() + " Stunde"
            if (hourValue != 1L) {
                hours += "n"
            }
        }
        var minutes = ""
        val minuteValue = TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.MILLISECONDS.toHours(time) * 60
        if (minuteValue != 0L) {
            minutes += " $minuteValue Minute"
            if (minuteValue != 1L) {
                minutes += "n"
            }
        }
        var seconds = ""
        val secondValue = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MILLISECONDS.toMinutes(time) * 60
        if (secondValue != 0L && hourValue == 0L) {
            seconds += " $secondValue Sekunde"
            if (secondValue != 1L) {
                seconds += "n"
            }
        }
        durationText!!.text = hours + minutes + seconds
        distanceText!!.text = String.format("%.2f", tourStatistics.walkedDistance / 1000.0).toString() + " km"
        visitedPOIsText!!.text = tourStatistics.visitedPOIs.toString()
    }

    /**
     * Disable the Back Button.
     */
    override fun onBackPressed() {
        return
    }
}
