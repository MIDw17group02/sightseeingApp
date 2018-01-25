package com.example.de.testssapplication

import android.content.Intent
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.view.View
import android.widget.Button

class MainActivity : WearableActivity() {

    private var snButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        snButton = findViewById<View>(R.id.sn_button) as Button
        snButton!!.setOnClickListener {
            val intent = Intent(applicationContext, DirectionNotifier::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

    }

    companion object {
        //Log-Tag
        var TAG = "Wear-MainActivity"
    }
}
