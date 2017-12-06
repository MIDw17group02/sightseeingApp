package com.example.de.testssapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * This is a very simple Activity which just displays a title screen and
 * fires up a new Configuration Activity if its button is clicked.
 */
public class StartActivity extends AppCompatActivity {

    private Button startTour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        startTour = findViewById(R.id.button);
        startTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent i = new Intent(getApplicationContext(), ConfigurationActivity.class);
                //For Testing Watch Sender/Receiver
                //Intent i = new Intent(getApplicationContext(), TestWatchSending.class);
                startActivity(i);
            }
        });
    }
}
