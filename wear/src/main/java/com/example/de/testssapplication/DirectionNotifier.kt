package com.example.de.testssapplication

import android.content.Context
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView

class DirectionNotifier : WearableActivity() {

    private var directionImage: ImageView? = null
    private var directionView: TextView? = null
    private var ditanceView: TextView? = null

    private var direction: String? = null
    private var distance: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direction_notifier)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        directionImage = findViewById<ImageView>(R.id.image)
        directionView = findViewById<TextView>(R.id.direction)
        ditanceView = findViewById<TextView>(R.id.distance)

        val intent = intent
        if (intent.hasExtra("dir") && intent.hasExtra("dis")) {
            direction = intent.getStringExtra("dir")
            distance = intent.getStringExtra("dis")
            val editor = getPreferences(Context.MODE_PRIVATE).edit()
            editor.putString("direction", direction)
            editor.putString("distance", distance)
            editor.apply()
            if (direction!!.length > 7) {
                directionView!!.text = direction
                ditanceView!!.text = ""
            } else {
                ditanceView!!.text = distance
            }
        } else {
            val prefs = getPreferences(Context.MODE_PRIVATE)
            direction = prefs.getString("direction", "undefined")
            distance = prefs.getString("distance", "undefined")
            directionView!!.text = direction
            ditanceView!!.text = distance
        }

        //set Image according direction
        if (direction != "undefined") {
            Log.d(TAG, "Setting image according direction \"" + direction + "\"")
            when (direction) {
                "ahead" -> directionImage!!.setImageResource(R.drawable.ic_ahead)
                "back" -> directionImage!!.setImageResource(R.drawable.ic_back)
                "hleft" -> directionImage!!.setImageResource(R.drawable.ic_hleft)
                "hright" -> directionImage!!.setImageResource(R.drawable.ic_hright)
                "left" -> directionImage!!.setImageResource(R.drawable.ic_left)
                "right" -> directionImage!!.setImageResource(R.drawable.ic_right)
                "sleft" -> directionImage!!.setImageResource(R.drawable.ic_sleft)
                "sright" -> directionImage!!.setImageResource(R.drawable.ic_sright)
                else -> {
                }
            }


        }

    }

    companion object {
        //Log-Tag
        var TAG = "Wear-DirectionNotifier"
    }

}
