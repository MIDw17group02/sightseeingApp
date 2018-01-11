package com.example.de.testssapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import model.DataModel;
import model.TourStatistics;

public class EndScreenActivity extends AppCompatActivity {

    private TextView distanceText;
    private TextView durationText;
    private TextView visitedPOIsText;

    private Button endTourButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_screen2);

        setTitle(getString(R.string.title_end_screen));

        distanceText = (TextView) findViewById(R.id.label_statistic_distance);
        durationText = (TextView) findViewById(R.id.label_statistic_duration);
        visitedPOIsText = (TextView) findViewById(R.id.label_poiCount);

        endTourButton = (Button) findViewById(R.id.endTourButton);
        endTourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Finish the App
                finishAndRemoveTask();
                finishAffinity();
            }
        });

        TourStatistics tourStatistics = DataModel.getInstance().getTourStatistics();
        tourStatistics.setWalkedDuration(System.currentTimeMillis() - tourStatistics.getWalkedDuration());
        long time = tourStatistics.getWalkedDuration();
        durationText.setText(String.valueOf(TimeUnit.MILLISECONDS.toHours(time)) + " Stunden, "
        + String.valueOf(TimeUnit.MILLISECONDS.toMinutes(time)) + " Minuten, "
        + String.valueOf(TimeUnit.MILLISECONDS.toSeconds(time)) + " Sekunden");
        distanceText.setText(String.valueOf(tourStatistics.getWalkedDistance()) + " km");
        visitedPOIsText.setText(String.valueOf(tourStatistics.getVisitedPOIs()));
    }
}
