package com.example.de.testssapplication

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

/**
 * This is a very simple Activity which just displays a title screen and
 * fires up a new Configuration Activity if its button is clicked.
 */
class StartActivity : AppCompatActivity() {

    private var startTour: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start2)

        startTour = findViewById(R.id.button)
        startTour!!.setOnClickListener {
            //finish()
            val i = Intent(applicationContext, ConfigurationActivity::class.java)
            //For Testing Watch Sender/Receiver
            //Intent i = new Intent(getApplicationContext(), TestWatchSending.class);
            startActivity(i)
        }
    }
}
